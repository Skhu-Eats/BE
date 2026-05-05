package com.skhueats.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EmailRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@skhu\\.ac\\.kr$", message = "성공회대학교 이메일(@skhu.ac.kr)만 사용할 수 있습니다")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}