package com.skhueats.post.entity;

import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;

public enum FoodCategory {
    KOREAN("한식"),
    STEW("찌개"),
    NOODLE("면류"),
    BUNSIK("분식"),
    JAPANESE("일식"),
    CHINESE("중식"),
    WESTERN("양식"),
    LIGHT_WESTERN("경양식");

    private final String label;

    FoodCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static FoodCategory from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        for (FoodCategory category : values()) {
            if (category.name().equalsIgnoreCase(normalized) || category.label.equals(normalized)) {
                return category;
            }
        }

        throw new ApiException(ErrorCode.INVALID_REQUEST, "food_category는 KOREAN/STEW/NOODLE/BUNSIK/JAPANESE/CHINESE/WESTERN/LIGHT_WESTERN만 허용됩니다.");
    }
}
