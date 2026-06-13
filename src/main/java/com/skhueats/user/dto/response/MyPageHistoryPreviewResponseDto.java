package com.skhueats.user.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.skhueats.post.entity.Participation;
import com.skhueats.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MyPageHistoryPreviewResponseDto {

    private String participationId;

    private String participationStatus;

    private String postId;

    private String title;

    private List<String> foodCategories;

    private String location;

    private LocalDateTime meetingTime;

    private Integer maxParticipants;

    private String postStatus;

    private String postStatusLabel;

    public static MyPageHistoryPreviewResponseDto of(Participation participation, List<String> foodCategories) {
        Post post = participation.getPost();

        return MyPageHistoryPreviewResponseDto.builder()
                .participationId(participation.getId())
                .participationStatus(participation.getStatus().name())
                .postId(post.getId())
                .title(post.getTitle())
                .foodCategories(foodCategories)
                .location(post.getLocation())
                .meetingTime(post.getMeetingTime())
                .maxParticipants(post.getMaxParticipants())
                .postStatus(post.getStatus().name())
                .postStatusLabel(post.getStatus().getDescription())
                .build();
    }
}
