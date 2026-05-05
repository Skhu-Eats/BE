package com.skhueats.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VerifyCodeRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@skhu\\.ac\\.kr$", message = "성공회대학교 이메일(@skhu.ac.kr)만 사용할 수 있습니다")
    private String email;

    @NotBlank(message = "인증코드는 필수입니다")
    @Size(min = 6, max = 6, message = "인증코드는 6자리여야 합니다")
    private String code;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}