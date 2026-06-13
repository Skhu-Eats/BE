package com.skhueats.user.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.skhueats.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MyPageResponseDto {

    private String userId;

    private String email;

    private String nickname;

    private String department;

    private Integer admissionYear;

    private String bio;

    private List<String> foodCategories;

    private String avatar;

    private Integer totalJoinCount;

    private Integer totalPostCount;

    private Integer mannerScore;

    private List<MyPageHistoryPreviewResponseDto> recentHistories;

    public static MyPageResponseDto of(
            User user,
            List<String> foodCategories,
            List<MyPageHistoryPreviewResponseDto> recentHistories
    ) {
        return MyPageResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .department(user.getDepartment())
                .admissionYear(user.getAdmissionYear())
                .bio(user.getBio())
                .foodCategories(foodCategories)
                .avatar(null)
                .totalJoinCount(user.getJoinCount())
                .totalPostCount(user.getPostCount())
                .mannerScore(user.getMannerScore())
                .recentHistories(recentHistories)
                .build();
    }
}
