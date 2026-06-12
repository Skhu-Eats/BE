package com.skhueats.post.service;

import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;
import com.skhueats.post.dto.request.CreatePostRequestDto;
import com.skhueats.post.dto.request.UpdatePostRequestDto;
import com.skhueats.post.dto.response.CreatePostResponseDto;
import com.skhueats.post.dto.response.PostDetailResponseDto;
import com.skhueats.post.dto.response.PostListResponseDto;
import com.skhueats.post.entity.Post;
import com.skhueats.post.entity.PostFoodCategory;
import com.skhueats.post.entity.PostStatus;
import com.skhueats.post.entity.TimeSlot;
import com.skhueats.post.repository.PostFoodCategoryRepository;
import com.skhueats.post.repository.PostRepository;
import com.skhueats.user.entity.User;
import com.skhueats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
    private final UserRepository userRepository;

    @Transactional
    public CreatePostResponseDto createPost(String email, CreatePostRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

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

    public List<PostListResponseDto> getPosts(String timeSlot, String status) {
        PostStatus statusFilter = resolveStatus(status);
        TimeSlot slot = TimeSlot.from(timeSlot);
        Integer startHour = (slot == null) ? null : slot.getStartHour();
        Integer endHour = (slot == null) ? null : slot.getEndHour();

        List<Post> posts = postRepository.findPostsByFilter(
                statusFilter.name(), startHour, endHour, LocalDateTime.now(KST_ZONE)
        );

        return createPostListResponse(posts);
    }

    public List<PostListResponseDto> getMyPosts(String email) {
        return getMyPosts(email, "host");
    }

    public List<PostListResponseDto> getMyPosts(String email, String role) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        MyPostRole myPostRole = resolveMyPostRole(role);
        if (myPostRole == MyPostRole.PARTICIPANT) {
            return List.of();
        }

        List<Post> posts = postRepository.findAllByHostActiveFirst(user.getId());

        return createPostListResponse(posts);
    }

    public PostDetailResponseDto getPost(String postId) {
        Post post = findPost(postId);
        List<String> foodCategories = findFoodCategories(post);

        return PostDetailResponseDto.of(post, foodCategories);
    }

    @Transactional
    public PostDetailResponseDto updatePost(String email, String postId, UpdatePostRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Post post = findPost(postId);

        validatePostHost(post, user);
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

        return PostDetailResponseDto.of(post, foodCategories);
    }

    @Transactional
    public void deletePost(String email, String postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Post post = findPost(postId);

        validatePostHost(post, user);

        postFoodCategoryRepository.deleteByPostId(post.getId());
        postRepository.delete(post);
        user.decreasePostCount();
    }

    private PostStatus resolveStatus(String status) {
        if (status == null || status.isBlank()) {
            return PostStatus.OPEN;
        }
        try {
            return PostStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "status는 open/closed/cancelled만 허용됩니다.");
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

    private List<String> normalizeFoodCategories(List<String> foodCategories) {
        return foodCategories.stream()
                .map(String::trim)
                .distinct()
                .toList();
    }

    private Post findPost(String postId) {
        return postRepository.findByIdWithHost(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
    }

    private List<String> findFoodCategories(Post post) {
        return postFoodCategoryRepository.findAllByPostId(post.getId()).stream()
                .map(PostFoodCategory::getCategory)
                .toList();
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

    private MyPostRole resolveMyPostRole(String role) {
        if (role == null || role.isBlank() || role.equalsIgnoreCase("host")) {
            return MyPostRole.HOST;
        }

        if (role.equalsIgnoreCase("participant")) {
            return MyPostRole.PARTICIPANT;
        }

        throw new ApiException(ErrorCode.INVALID_REQUEST, "role은 host 또는 participant만 사용할 수 있습니다.");
    }

    private enum MyPostRole {
        HOST,
        PARTICIPANT
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
