package com.skhueats.auth.controller;

import com.skhueats.auth.dto.request.EmailRequest;
import com.skhueats.auth.dto.request.VerifyCodeRequest;
import com.skhueats.auth.dto.response.CheckNicknameResponseDto;
import com.skhueats.auth.dto.response.RegisterResponseDto;
import com.skhueats.auth.service.AuthService;
import com.skhueats.auth.dto.request.RegisterRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-code")
    public Map<String, String> sendCode(@Valid @RequestBody EmailRequest request) {
        authService.sendVerificationCode(request.getEmail());
        return Map.of("message", "인증코드 발송 완료");
    }

    @PostMapping("/verify-code")
    public Map<String, String> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        authService.verifyCode(request.getEmail(), request.getCode());
        return Map.of("message", "이메일 인증 완료");
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        RegisterResponseDto response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<CheckNicknameResponseDto> checkNickname(
            @RequestParam String nickname
    ) {
        CheckNicknameResponseDto response = authService.checkNickname(nickname);
        return ResponseEntity.ok(response);
    }
}

