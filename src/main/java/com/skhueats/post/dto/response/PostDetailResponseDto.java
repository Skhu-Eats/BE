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
public class PostDetailResponseDto {

    private String postId;

    private String hostId;

    private String hostNickname;

    private String hostDepartment;

    private Integer hostAdmissionYear;

    private Integer hostMannerScore;

    private String title;

    private List<String> foodCategories;

    private String location;

    private LocalDateTime meetingTime;

    private LocalDateTime deadline;

    private Integer maxParticipants;

    private Integer currentParticipants;

    private String memo;

    private String kakaoLink;

    private Boolean kakaoLinkVisible;

    private List<PostParticipantResponseDto> participants;

    private String status;

    private String statusLabel;

    private String joinStatus;

    private String joinButtonLabel;

    private Boolean canJoin;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static PostDetailResponseDto of(Post post, List<String> foodCategories) {
        return of(post, foodCategories, null, List.of(), false);
    }

    public static PostDetailResponseDto of(
            Post post,
            List<String> foodCategories,
            PostJoinStatus joinStatus
    ) {
        return of(post, foodCategories, joinStatus, List.of(), false);
    }

    public static PostDetailResponseDto of(
            Post post,
            List<String> foodCategories,
            PostJoinStatus joinStatus,
            List<PostParticipantResponseDto> participants,
            boolean kakaoLinkVisible
    ) {
        PostResponseStatus responseStatus = PostResponseStatus.from(post);

        PostDetailResponseDtoBuilder builder = PostDetailResponseDto.builder()
                .postId(post.getId())
                .hostId(post.getHost().getId())
                .hostNickname(post.getHost().getNickname())
                .hostDepartment(post.getHost().getDepartment())
                .hostAdmissionYear(post.getHost().getAdmissionYear())
                .hostMannerScore(post.getHost().getMannerScore())
                .title(post.getTitle())
                .foodCategories(foodCategories)
                .location(post.getLocation())
                .meetingTime(post.getMeetingTime())
                .deadline(post.getDeadline())
                .maxParticipants(post.getMaxParticipants())
                .currentParticipants(post.getCurrentParticipants())
                .memo(post.getMemo())
                .kakaoLink(kakaoLinkVisible ? post.getKakaoLink() : null)
                .kakaoLinkVisible(kakaoLinkVisible)
                .participants(participants)
                .status(responseStatus.name())
                .statusLabel(responseStatus.getDescription())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt());

        if (joinStatus != null) {
            builder.joinStatus(joinStatus.name())
                    .joinButtonLabel(joinStatus.getButtonLabel())
                    .canJoin(joinStatus.canJoin());
        }

        return builder.build();
    }
}
