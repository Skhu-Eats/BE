package com.skhueats.auth.controller;

import com.skhueats.auth.dto.EmailRequest;
import com.skhueats.auth.dto.VerifyCodeRequest;
import com.skhueats.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-code")
    public String sendCode(@RequestBody EmailRequest request) {
        authService.sendVerificationCode(request.getEmail());
        return "인증코드 발송 완료";
    }

    @PostMapping("/verify-code")
    public String verifyCode(@RequestBody VerifyCodeRequest request) {
        authService.verifyCode(request.getEmail(), request.getCode());
        return "이메일 인증 완료";
    }
}