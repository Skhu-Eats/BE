package com.skhueats.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.skhueats.user.entity.User;

import java.util.List;

public record MyProfileResponseDto(

        @JsonProperty("user_id")
        String userId,

        String email,

        String nickname,

        String department,

        @JsonProperty("admission_year")
        Integer admissionYear,

        String bio,

        @JsonProperty("food_categories")
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
