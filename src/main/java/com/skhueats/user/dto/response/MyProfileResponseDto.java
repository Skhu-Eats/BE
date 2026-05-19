package com.skhueats.user.dto.response;

import com.skhueats.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyProfileResponseDto {

    private String userId;
    private String email;
    private String nickname;

    public static MyProfileResponseDto from(User user) {
        return MyProfileResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}