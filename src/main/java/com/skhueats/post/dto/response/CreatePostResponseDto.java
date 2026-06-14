package com.skhueats.post.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.skhueats.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreatePostResponseDto {

    private String postId;

    private String hostId;

    private String hostNickname;

    private String title;

    private List<String> foodCategories;

    private String location;

    private LocalDateTime meetingTime;

    private LocalDateTime deadline;

    private Integer maxParticipants;

    private Integer currentParticipants;

    private String memo;

    private String kakaoLink;

    private String status;

    private String statusLabel;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static CreatePostResponseDto of(Post post, List<String> foodCategories) {
        PostResponseStatus responseStatus = PostResponseStatus.from(post);

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
                .status(responseStatus.name())
                .statusLabel(responseStatus.getDescription())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
