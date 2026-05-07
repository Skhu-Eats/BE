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
            regexp = "^[A-Za-z0-9._%+-]+@(office\\.)?skhu\\.ac\\.kr$",
            message = "성공회대학교 이메일(@skhu.ac.kr 또는 @office.skhu.ac.kr)만 사용할 수 있습니다"
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
    @Pattern(
            regexp = "^[가-힣A-Za-z0-9_]+$",
            message = "닉네임은 한글, 영문, 숫자, 밑줄(_)만 사용할 수 있습니다"
    )
    private String nickname;

    @NotBlank(message = "학과는 필수입니다")
    @Size(max = 50, message = "학과는 50자 이하여야 합니다")
    @Pattern(
            regexp = "^[가-힣A-Za-z\\s]+$",
            message = "학과는 한글, 영문만 입력할 수 있습니다"
    )
    private String department;

    @NotNull(message = "입학년도는 필수입니다")
    @Min(value = 1900, message = "입학년도는 1900년 이상이어야 합니다")
    @Max(value = 2099, message = "입학년도는 2099년 이하여야 합니다")
    private Integer admissionYear;

    @Size(max = 100, message = "자기소개는 100자 이하여야 합니다")
    private String bio;
}