package com.skhueats.auth.controller;

import com.skhueats.auth.dto.request.EmailRequest;
import com.skhueats.auth.dto.request.LoginRequest;
import com.skhueats.auth.dto.request.LogoutRequest;
import com.skhueats.auth.dto.request.PasswordResetRequestDto;
import com.skhueats.auth.dto.request.PasswordResetSendCodeRequestDto;
import com.skhueats.auth.dto.request.PasswordResetVerifyCodeRequestDto;
import com.skhueats.auth.dto.request.RegisterRequestDto;
import com.skhueats.auth.dto.request.TokenRefreshRequest;
import com.skhueats.auth.dto.request.VerifyCodeRequest;
import com.skhueats.auth.dto.response.CheckNicknameResponseDto;
import com.skhueats.auth.dto.response.LoginResponse;
import com.skhueats.auth.dto.response.RegisterResponseDto;
import com.skhueats.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/password/reset/send-code")
    public ResponseEntity<Map<String, String>> sendPasswordResetCode(
            @Valid @RequestBody PasswordResetSendCodeRequestDto request
    ) {
        authService.sendPasswordResetCode(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 인증번호 발송 완료"));
    }

    @PostMapping("/password/reset/verify-code")
    public ResponseEntity<Map<String, String>> verifyPasswordResetCode(
            @Valid @RequestBody PasswordResetVerifyCodeRequestDto request
    ) {
        authService.verifyPasswordResetCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 인증 완료"));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordResetRequestDto request
    ) {
        authService.resetPassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 완료"));
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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<CheckNicknameResponseDto> checkNickname(
            @RequestParam String nickname
    ) {
        CheckNicknameResponseDto response = authService.checkNickname(nickname);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        LoginResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }
}
