package com.skhueats.global.response;

import com.skhueats.global.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ErrorResponse {

    private int status;
    private String code;
    private String message;
    private String path;

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.name())
                .message(message)
                .path(path)
                .build();
    }
}