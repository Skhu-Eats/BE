package com.skhueats.post.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.skhueats.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CreatePostResponseDto {

    @JsonProperty("post_id")
    private String postId;

    @JsonProperty("host_id")
    private String hostId;

    @JsonProperty("host_nickname")
    private String hostNickname;

    private String title;

    @JsonProperty("food_categories")
    private List<String> foodCategories;

    private String location;

    @JsonProperty("meeting_time")
    private LocalDateTime meetingTime;

    private LocalDateTime deadline;

    @JsonProperty("max_participants")
    private Integer maxParticipants;

    @JsonProperty("current_participants")
    private Integer currentParticipants;

    private String memo;

    @JsonProperty("kakao_link")
    private String kakaoLink;

    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static CreatePostResponseDto of(Post post, List<String> foodCategories) {
        return CreatePostResponseDto.builder()
                .postId(post.getId())
                .hostId(post.getHost().getId())
                .hostNickname(post.getHost().getNickname())
                .title(post.getTitle())
                .foodCategories(foodCategories)
                .location(post.getLocation())
                .meetingTime(post.getMeetingTime())
                .deadline(post.getDeadline())
                .maxParticipants(post.getMaxParticipants())
                .currentParticipants(post.getCurrentParticipants())
                .memo(post.getMemo())
                .kakaoLink(post.getKakaoLink())
                .status(post.getStatus().name())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
