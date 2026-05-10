package com.skhueats.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckNicknameResponseDto {

    private boolean available;
    private String message;
}