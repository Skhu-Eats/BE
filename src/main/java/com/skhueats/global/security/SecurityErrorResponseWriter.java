package com.skhueats.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skhueats.global.exception.ErrorCode;
import com.skhueats.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletRequest request,
                      HttpServletResponse response,
                      ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                errorCode.getMessage(),
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}