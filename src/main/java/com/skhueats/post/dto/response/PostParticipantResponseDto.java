package com.skhueats.post.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.skhueats.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PostParticipantResponseDto {

    private String userId;

    private String nickname;

    private String department;

    private Integer admissionYear;

    private Integer mannerScore;

    public static PostParticipantResponseDto of(User user) {
        return PostParticipantResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .department(user.getDepartment())
                .admissionYear(user.getAdmissionYear())
                .mannerScore(user.getMannerScore())
                .build();
    }
}
