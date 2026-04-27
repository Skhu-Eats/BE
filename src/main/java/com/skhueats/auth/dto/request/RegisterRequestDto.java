package com.skhueats.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDto {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@skhu\\.ac\\.kr$",
            message = "성공회대학교 이메일(@skhu.ac.kr)만 사용할 수 있습니다"
    )
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+=-]{8,20}$",
            message = "비밀번호는 8~20자이며 영문과 숫자를 포함해야 합니다"
    )
    private String password;

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다")
    private String nickname;

    @NotBlank(message = "학과는 필수입니다")
    @Size(max = 50, message = "학과는 50자 이하여야 합니다")
    private String department;

    @NotBlank(message = "입학년도는 필수입니다")
    @Pattern(regexp = "\\d{4}", message = "입학년도는 4자리 숫자여야 합니다")
    private String admissionYear;

    @Size(max = 100, message = "자기소개는 100자 이하여야 합니다")
    private String bio;
}