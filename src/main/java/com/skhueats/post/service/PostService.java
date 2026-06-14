package com.skhueats.post.service;

import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;
import com.skhueats.notification.entity.NotificationType;
import com.skhueats.notification.service.NotificationService;
import com.skhueats.post.dto.request.CreatePostRequestDto;
import com.skhueats.post.dto.request.UpdatePostRequestDto;
import com.skhueats.post.dto.response.CreatePostResponseDto;
import com.skhueats.post.dto.response.JoinPostResponseDto;
import com.skhueats.post.dto.response.ParticipationHistoryPageResponseDto;
import com.skhueats.post.dto.response.ParticipationHistoryResponseDto;
import com.skhueats.post.dto.response.PostDetailResponseDto;
import com.skhueats.post.dto.response.PostJoinStatus;
import com.skhueats.post.dto.response.PostListResponseDto;
import com.skhueats.post.dto.response.PostParticipantResponseDto;
import com.skhueats.post.dto.response.PostResponseStatus;
import com.skhueats.post.entity.FoodCategory;
import com.skhueats.post.entity.Participation;
import com.skhueats.post.entity.ParticipationStatus;
import com.skhueats.post.entity.Post;
import com.skhueats.post.entity.PostFoodCategory;
import com.skhueats.post.entity.PostStatus;
import com.skhueats.post.entity.TimeSlot;
import com.skhueats.post.repository.ParticipationRepository;
import com.skhueats.post.repository.PostFoodCategoryRepository;
import com.skhueats.post.repository.PostRepository;
import com.skhueats.user.entity.User;
import com.skhueats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private static final int DAILY_POST_LIMIT = 3;
    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    private final PostRepository postRepository;
    private final PostFoodCategoryRepository postFoodCategoryRepository;
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public CreatePostResponseDto createPost(String email, CreatePostRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        validateMeetingTimeFuture(requestDto.getMeetingTime());
        validateDailyPostLimit(user);

        Post post = new Post(
                user,
                requestDto.getTitle(),
                requestDto.getLocation(),
                requestDto.getMeetingTime(),
                requestDto.getMaxParticipants(),
                requestDto.getMemo(),
                requestDto.getKakaoLink()
        );

        Post savedPost = postRepository.save(post);

        List<String> foodCategories = normalizeFoodCategories(requestDto.getFoodCategories());

        List<PostFoodCategory> postFoodCategories = foodCategories.stream()
                .map(category -> new PostFoodCategory(savedPost, category))
                .toList();

        postFoodCategoryRepository.saveAll(postFoodCategories);

        user.increasePostCount();

        return CreatePostResponseDto.of(savedPost, foodCategories);
    }

    public List<PostListResponseDto> getPosts(
            String timeSlot,
            String status,
            String location,
            Integer maxParticipants,
            FoodCategory foodCategory
    ) {
        PostResponseStatus statusFilter = resolveStatus(status);
        TimeSlot slot = TimeSlot.from(timeSlot);
        validateMaxParticipantsFilter(maxParticipants);
        Integer startHour = (slot == null) ? null : slot.getStartHour();
        Integer endHour = (slot == null) ? null : slot.getEndHour();

        List<Post> posts = postRepository.findPostsByFilter(
                startHour,
                endHour,
                normalizeNullable(location),
                maxParticipants,
                foodCategory == null ? null : foodCategory.getLabel()
        );

        if (statusFilter != null) {
            LocalDateTime now = LocalDateTime.now(KST_ZONE);
            posts = posts.stream()
                    .filter(post -> PostResponseStatus.from(post, now) == statusFilter)
                    .toList();
        }

        return createPostListResponse(posts);
    }

    public List<PostListResponseDto> getMyPosts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        List<Post> posts = postRepository.findAllByHostActiveFirst(user.getId());

        return createPostListResponse(posts);
    }

    public ParticipationHistoryPageResponseDto getMyHistory(String email, int page, int limit) {
        validatePageRequest(page, limit);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        PageRequest pageRequest = PageRequest.of(page - 1, limit);
        Page<Participation> participationPage = participationRepository.findHistoryByUserId(
                user.getId(),
                ParticipationStatus.JOINED,
                pageRequest
        );

        List<Participation> participations = participationPage.getContent();
        Map<String, List<String>> categoriesByPostId = findFoodCategoriesByPostId(participations);

        LocalDateTime now = LocalDateTime.now(KST_ZONE);
        List<ParticipationHistoryResponseDto> data = participations.stream()
                .map(participation -> ParticipationHistoryResponseDto.of(
                        participation,
                        categoriesByPostId.getOrDefault(participation.getPost().getId(), List.of()),
                        canCancelParticipation(participation, now)
                ))
                .toList();

        return ParticipationHistoryPageResponseDto.builder()
                .totalCount(participationPage.getTotalElements())
                .page(page)
                .limit(limit)
                .data(data)
                .build();
    }

    public PostDetailResponseDto getPost(String email, String postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Post post = findPost(postId);
        List<String> foodCategories = findFoodCategories(post);
        PostJoinStatus joinStatus = resolveJoinStatus(post, user);
        List<PostParticipantResponseDto> participants = findParticipants(post);
        boolean kakaoLinkVisible = canViewKakaoLink(post, user);

        return PostDetailResponseDto.of(post, foodCategories, joinStatus, participants, kakaoLinkVisible);
    }

    public PostDetailResponseDto getPost(String postId) {
        Post post = findPost(postId);
        List<String> foodCategories = findFoodCategories(post);

        return PostDetailResponseDto.of(post, foodCategories);
    }

    @Transactional
    public JoinPostResponseDto joinPost(String email, String postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Post post = findPostForUpdate(postId);

        validateJoinable(post, user);

        Participation participation = participationRepository.findByPostAndUser(post, user)
                .map(existing -> {
                    existing.rejoin();
                    return existing;
                })
                .orElseGet(() -> new Participation(post, user));
        participationRepository.save(participation);

        post.join();
        user.increaseJoinCount();

        notificationService.createNotification(
                post.getHost(),
                NotificationType.POST_JOIN,
                "새로운 참여 신청",
                user.getNickname() + "님이 '" + post.getTitle() + "' 모임에 참여했어요.",
                "POST",
                post.getId()
        );

        return JoinPostResponseDto.of(post, PostJoinStatus.JOINED);
    }

    @Transactional
    public void cancelJoin(String email, String postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Post post = findPostForUpdate(postId);

        if (post.isHostedBy(user)) {
            throw new ApiException(ErrorCode.POST_FORBIDDEN);
        }

        Participation participation = participationRepository.findByPostAndUser(post, user)
                .filter(Participation::isJoined)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_JOINED));

        if (post.getStatus() == PostStatus.CANCELLED) {
            throw new ApiException(ErrorCode.POST_RECRUITMENT_CLOSED);
        }

        validateJoinCancelable(post);

        participation.cancel();
        post.cancelJoin();
        user.decreaseJoinCount();

        notificationService.createNotification(
                post.getHost(),
                NotificationType.POST_LEAVE,
                "모임 참여 취소",
                user.getNickname() + "님이 '" + post.getTitle() + "' 모임 참여를 취소했어요.",
                "POST",
                post.getId()
        );
    }

    @Transactional
    public PostDetailResponseDto updatePost(String email, String postId, UpdatePostRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Post post = findPost(postId);

        validatePostHost(post, user);
        validateMeetingTimeFuture(requestDto.getMeetingTime());
        validateMaxParticipants(post, requestDto.getMaxParticipants());

        post.update(
                requestDto.getTitle(),
                requestDto.getLocation(),
                requestDto.getMeetingTime(),
                requestDto.getMaxParticipants(),
                requestDto.getMemo(),
                requestDto.getKakaoLink()
        );

        List<String> foodCategories = normalizeFoodCategories(requestDto.getFoodCategories());
        replaceFoodCategories(post, foodCategories);

        return PostDetailResponseDto.of(post, foodCategories, null, findParticipants(post), true);
    }

    @Transactional
    public void deletePost(String email, String postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Post post = findPostForUpdate(postId);

        validatePostHost(post, user);

        List<Participation> participants = participationRepository.findAllByPostAndStatusWithUser(
                post,
                ParticipationStatus.JOINED
        );

        participants.forEach(participation -> {
            User participant = participation.getUser();
            participant.decreaseJoinCount();
            notificationService.createNotification(
                    participant,
                    NotificationType.POST_CANCELLED,
                    "참여한 모임이 취소되었어요",
                    post.getTitle() + " 모임이 모집자에 의해 취소되었습니다.",
                    null,
                    null
            );
        });

        postFoodCategoryRepository.deleteByPostId(post.getId());
        postRepository.delete(post);
        user.decreasePostCount();
    }

    private PostResponseStatus resolveStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return PostResponseStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "status는 open/closing_soon/closed/cancelled만 허용됩니다.");
        }
    }

    private void validateDailyPostLimit(User user) {
        LocalDate today = LocalDate.now(KST_ZONE);

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        long todayPostCount = postRepository.countByHostAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                user,
                startOfDay,
                startOfNextDay
        );

        if (todayPostCount >= DAILY_POST_LIMIT) {
            throw new ApiException(ErrorCode.POST_DAILY_LIMIT_EXCEEDED);
        }
    }

    private void validateMeetingTimeFuture(LocalDateTime meetingTime) {
        if (meetingTime == null) {
            return;
        }

        if (!meetingTime.isAfter(LocalDateTime.now(KST_ZONE))) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "모임 시간은 현재 시간 이후여야 합니다.");
        }
    }

    private List<String> normalizeFoodCategories(List<String> foodCategories) {
        return foodCategories.stream()
                .map(String::trim)
                .map(FoodCategory::from)
                .map(FoodCategory::getLabel)
                .distinct()
                .toList();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private Post findPost(String postId) {
        return postRepository.findByIdWithHost(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
    }

    private Post findPostForUpdate(String postId) {
        return postRepository.findByIdWithHostForUpdate(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
    }

    private List<String> findFoodCategories(Post post) {
        return postFoodCategoryRepository.findAllByPostId(post.getId()).stream()
                .map(PostFoodCategory::getCategory)
                .toList();
    }

    private List<PostParticipantResponseDto> findParticipants(Post post) {
        return participationRepository.findAllByPostIdAndStatusWithUser(post.getId(), ParticipationStatus.JOINED)
                .stream()
                .map(participation -> PostParticipantResponseDto.of(participation.getUser()))
                .toList();
    }

    private Map<String, List<String>> findFoodCategoriesByPostId(List<Participation> participations) {
        if (participations.isEmpty()) {
            return Map.of();
        }

        List<String> postIds = participations.stream()
                .map(participation -> participation.getPost().getId())
                .toList();

        return postFoodCategoryRepository.findAllByPostIdIn(postIds).stream()
                .collect(Collectors.groupingBy(
                        postFoodCategory -> postFoodCategory.getPost().getId(),
                        Collectors.mapping(PostFoodCategory::getCategory, Collectors.toList())
                ));
    }

    private List<PostListResponseDto> createPostListResponse(List<Post> posts) {
        if (posts.isEmpty()) {
            return List.of();
        }

        List<String> postIds = posts.stream()
                .map(Post::getId)
                .toList();

        Map<String, List<String>> categoriesByPostId = postFoodCategoryRepository.findAllByPostIdIn(postIds).stream()
                .collect(Collectors.groupingBy(
                        postFoodCategory -> postFoodCategory.getPost().getId(),
                        Collectors.mapping(PostFoodCategory::getCategory, Collectors.toList())
                ));

        return posts.stream()
                .map(post -> PostListResponseDto.of(
                        post,
                        categoriesByPostId.getOrDefault(post.getId(), List.of())
                ))
                .toList();
    }

    private PostJoinStatus resolveJoinStatus(Post post, User user) {
        if (post.isHostedBy(user)) {
            return PostJoinStatus.HOST;
        }

        if (participationRepository.existsByPostAndUserAndStatus(post, user, ParticipationStatus.JOINED)) {
            return PostJoinStatus.JOINED;
        }

        if (isDeadlinePassed(post) || post.isClosed() || post.isFull()) {
            return PostJoinStatus.FULL;
        }

        return PostJoinStatus.AVAILABLE;
    }

    private boolean canViewKakaoLink(Post post, User user) {
        return post.isHostedBy(user)
                || participationRepository.existsByPostAndUserAndStatus(post, user, ParticipationStatus.JOINED);
    }

    private void validateJoinable(Post post, User user) {
        if (post.isHostedBy(user)) {
            throw new ApiException(ErrorCode.POST_SELF_JOIN_NOT_ALLOWED);
        }

        if (participationRepository.existsByPostAndUserAndStatus(post, user, ParticipationStatus.JOINED)) {
            throw new ApiException(ErrorCode.POST_ALREADY_JOINED);
        }

        if (isDeadlinePassed(post)) {
            throw new ApiException(ErrorCode.POST_RECRUITMENT_CLOSED);
        }

        if (post.isClosed()) {
            throw new ApiException(ErrorCode.POST_RECRUITMENT_CLOSED);
        }

        if (post.isFull()) {
            throw new ApiException(ErrorCode.POST_FULL);
        }
    }

    private void validateJoinCancelable(Post post) {
        LocalDateTime cancelDeadline = post.getMeetingTime().minusMinutes(30);

        if (!LocalDateTime.now(KST_ZONE).isBefore(cancelDeadline)) {
            throw new ApiException(ErrorCode.POST_CANCEL_TIME_EXPIRED);
        }
    }

    private boolean canCancelParticipation(Participation participation, LocalDateTime now) {
        Post post = participation.getPost();
        LocalDateTime cancelDeadline = post.getMeetingTime().minusMinutes(30);

        return participation.isJoined()
                && post.getStatus() != PostStatus.CANCELLED
                && now.isBefore(cancelDeadline);
    }

    private void validatePageRequest(int page, int limit) {
        if (page < 1 || limit < 1) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "page와 limit은 1 이상이어야 합니다.");
        }
    }

    private void validateMaxParticipantsFilter(Integer maxParticipants) {
        if (maxParticipants != null && (maxParticipants < 2 || maxParticipants > 4)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "max_participants는 2~4만 허용됩니다.");
        }
    }

    private boolean isDeadlinePassed(Post post) {
        return post.getDeadline().isBefore(LocalDateTime.now(KST_ZONE));
    }

    private void validatePostHost(Post post, User user) {
        if (!post.getHost().getId().equals(user.getId())) {
            throw new ApiException(ErrorCode.POST_FORBIDDEN);
        }
    }

    private void validateMaxParticipants(Post post, Integer maxParticipants) {
        if (maxParticipants < post.getCurrentParticipants()) {
            throw new ApiException(
                    ErrorCode.INVALID_REQUEST,
                    "최대 참가 인원은 현재 참가 인원보다 적을 수 없습니다."
            );
        }
    }

    private void replaceFoodCategories(Post post, List<String> foodCategories) {
        postFoodCategoryRepository.deleteByPostId(post.getId());

        List<PostFoodCategory> postFoodCategories = foodCategories.stream()
                .map(category -> new PostFoodCategory(post, category))
                .toList();

        postFoodCategoryRepository.saveAll(postFoodCategories);
    }
}
