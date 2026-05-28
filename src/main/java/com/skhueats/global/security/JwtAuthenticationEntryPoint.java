package com.skhueats.global.security;

import com.skhueats.global.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    public JwtAuthenticationEntryPoint(SecurityErrorResponseWriter securityErrorResponseWriter) {
        this.securityErrorResponseWriter = securityErrorResponseWriter;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {
        securityErrorResponseWriter.write(request, response, ErrorCode.ACCESS_TOKEN_REQUIRED);
    }
}