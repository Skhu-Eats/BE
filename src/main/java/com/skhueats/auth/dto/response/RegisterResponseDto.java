package com.skhueats.auth.dto.response;

import com.skhueats.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponseDto {

    private String message;
    private String userId;
    private String email;
    private String nickname;
    private String department;
    private Integer admissionYear;
    private String bio;
    private Boolean emailVerified;

    public static RegisterResponseDto from(User user) {
        return new RegisterResponseDto(
                "회원가입 완료",
                user.getId().toString(),
                user.getEmail(),
                user.getNickname(),
                user.getDepartment(),
                user.getAdmissionYear(),
                user.getBio(),
                user.isEmailVerified()
        );    }
}