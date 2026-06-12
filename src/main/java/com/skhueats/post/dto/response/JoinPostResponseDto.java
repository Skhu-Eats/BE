package com.skhueats.post.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.skhueats.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JoinPostResponseDto {

    private String postId;

    private Integer currentParticipants;

    private Integer maxParticipants;

    private String status;

    private String statusLabel;

    private String joinStatus;

    private String joinButtonLabel;

    private Boolean canJoin;

    public static JoinPostResponseDto of(Post post, PostJoinStatus joinStatus) {
        return JoinPostResponseDto.builder()
                .postId(post.getId())
                .currentParticipants(post.getCurrentParticipants())
                .maxParticipants(post.getMaxParticipants())
                .status(post.getStatus().name())
                .statusLabel(post.getStatus().getDescription())
                .joinStatus(joinStatus.name())
                .joinButtonLabel(joinStatus.getButtonLabel())
                .canJoin(joinStatus.canJoin())
                .build();
    }
}
