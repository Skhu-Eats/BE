package com.skhueats.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh Token을 전달해주세요")
    private String refreshToken;
}
