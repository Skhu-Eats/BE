package com.skhueats.user.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.skhueats.user.entity.User;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MyProfileResponseDto(

        String userId,

        String email,

        String nickname,

        String department,

        Integer admissionYear,

        String bio,

        List<String> foodCategories,

        String avatar
) {

    public static MyProfileResponseDto from(User user, List<String> foodCategories) {
        return new MyProfileResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getDepartment(),
                user.getAdmissionYear(),
                user.getBio(),
                foodCategories,
                null
        );
    }
}
