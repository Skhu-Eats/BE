package com.skhueats.user.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.skhueats.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MyProfileResponseDto {

    private String userId;
    private String email;
    private String nickname;

    public static MyProfileResponseDto from(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        return MyProfileResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
