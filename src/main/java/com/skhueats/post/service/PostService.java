package com.skhueats.post.service;

import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;
import com.skhueats.post.dto.request.CreatePostRequestDto;
import com.skhueats.post.dto.response.CreatePostResponseDto;
import com.skhueats.post.dto.response.PostListResponseDto;
import com.skhueats.post.entity.Post;
import com.skhueats.post.entity.PostFoodCategory;
import com.skhueats.post.entity.PostStatus;
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
        Integer startHour = resolveStartHour(timeSlot);
        Integer endHour = resolveEndHour(timeSlot);

        List<Post> posts = postRepository.findPostsByFilter(statusFilter, startHour, endHour);
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

    private Integer resolveStartHour(String timeSlot) {
        if (isAllTimeSlot(timeSlot)) {
            return null;
        }
        return switch (timeSlot.trim().toLowerCase()) {
            case "lunch" -> 11;
            case "dinner" -> 17;
            default -> throw new ApiException(ErrorCode.INVALID_REQUEST, "time_slot은 lunch/dinner/all만 허용됩니다.");
        };
    }

    private Integer resolveEndHour(String timeSlot) {
        if (isAllTimeSlot(timeSlot)) {
            return null;
        }
        return switch (timeSlot.trim().toLowerCase()) {
            case "lunch" -> 14;
            case "dinner" -> 20;
            default -> throw new ApiException(ErrorCode.INVALID_REQUEST, "time_slot은 lunch/dinner/all만 허용됩니다.");
        };
    }

    private boolean isAllTimeSlot(String timeSlot) {
        return timeSlot == null || timeSlot.isBlank() || timeSlot.trim().equalsIgnoreCase("all");
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
}
