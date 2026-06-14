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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(
        name = "인증/계정 (Auth)",
        description = "회원가입, 이메일 인증, 로그인, 비밀번호 재설정, 토큰 재발급/로그아웃. "
                + "이 그룹의 모든 API는 인증 토큰 없이 호출합니다."
)
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "[회원가입] 이메일 인증코드 발송",
            description = """
                    회원가입을 위한 6자리 인증코드를 학교 이메일로 발송합니다.
                    - 성공회대 이메일(@skhu.ac.kr 또는 @office.skhu.ac.kr)만 허용합니다.
                    - 이미 가입된 이메일이면 발송하지 않고 409를 반환합니다.
                    - 발송된 코드는 일정 시간 후 만료되며, 동일 이메일은 1분 내 재요청이 제한될 수 있습니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증코드 발송 완료"),
            @ApiResponse(responseCode = "400", description = "학교 이메일 형식이 아님 (AUTH_400_EMAIL)"),
            @ApiResponse(responseCode = "409", description = "이미 가입된 이메일 (USER_409_EMAIL)"),
            @ApiResponse(responseCode = "429", description = "재발송 제한 (AUTH_429_RESEND)")
    })
    @PostMapping("/send-code")
    public Map<String, String> sendCode(@Valid @RequestBody EmailRequest request) {
        authService.sendVerificationCode(request.getEmail());
        return Map.of("message", "인증코드 발송 완료");
    }

    @Operation(
            summary = "[비밀번호 재설정] 인증코드 발송",
            description = """
                    가입된 학교 이메일로 비밀번호 재설정 인증코드를 발송합니다.
                    - 보안을 위해 미가입 이메일이어도 200을 반환합니다(가입 여부를 노출하지 않음).
                    - 인증 실패 누적으로 잠긴 경우 또는 재발송 제한 시간 내에는 429를 반환합니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증번호 발송 완료(또는 미가입 이메일)"),
            @ApiResponse(responseCode = "400", description = "학교 이메일 형식이 아님 (AUTH_400_EMAIL)"),
            @ApiResponse(responseCode = "429", description = "인증 시도 잠금 / 재발송 제한 (AUTH_429_LOCK, AUTH_429_RESEND)")
    })
    @PostMapping("/password/reset/send-code")
    public ResponseEntity<Map<String, String>> sendPasswordResetCode(
            @Valid @RequestBody PasswordResetSendCodeRequestDto request
    ) {
        authService.sendPasswordResetCode(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 인증번호 발송 완료"));
    }

    @Operation(
            summary = "[비밀번호 재설정] 인증코드 검증",
            description = """
                    비밀번호 재설정 인증코드를 검증합니다.
                    - 검증에 성공하면 해당 이메일이 '재설정 인증 완료' 상태가 되어, 이후 비밀번호 재설정 API를 호출할 수 있습니다.
                    - 코드가 없거나 만료된 경우 400, 코드가 일치하지 않으면 400을 반환합니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 완료"),
            @ApiResponse(responseCode = "400", description = "코드 만료/없음 또는 불일치 (AUTH_400_EXPIRED, AUTH_400_MISMATCH)"),
            @ApiResponse(responseCode = "429", description = "인증 시도 잠금 (AUTH_429_LOCK)")
    })
    @PostMapping("/password/reset/verify-code")
    public ResponseEntity<Map<String, String>> verifyPasswordResetCode(
            @Valid @RequestBody PasswordResetVerifyCodeRequestDto request
    ) {
        authService.verifyPasswordResetCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 인증 완료"));
    }

    @Operation(
            summary = "[비밀번호 재설정] 새 비밀번호 변경",
            description = """
                    재설정 인증이 완료된 이메일에 한해 새 비밀번호로 변경합니다.
                    - 인증코드 검증(/password/reset/verify-code)을 먼저 통과해야 합니다.
                    - 변경 시 해당 계정의 기존 Refresh Token을 모두 무효화합니다(전체 로그아웃 효과).
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 재설정 완료"),
            @ApiResponse(responseCode = "403", description = "재설정 인증 미완료 (AUTH_403)"),
            @ApiResponse(responseCode = "404", description = "사용자 없음 (USER_404)")
    })
    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordResetRequestDto request
    ) {
        authService.resetPassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 완료"));
    }

    @Operation(
            summary = "[회원가입] 이메일 인증코드 검증",
            description = """
                    회원가입용 이메일 인증코드를 검증합니다.
                    - 성공 시 해당 이메일이 '인증 완료' 상태가 되어, 이후 회원가입 API(/register)를 호출할 수 있습니다.
                    - 코드가 일치하지 않으면 400을 반환합니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 인증 완료"),
            @ApiResponse(responseCode = "400", description = "인증코드 불일치 (AUTH_400_MISMATCH)")
    })
    @PostMapping("/verify-code")
    public Map<String, String> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        authService.verifyCode(request.getEmail(), request.getCode());
        return Map.of("message", "이메일 인증 완료");
    }

    @Operation(
            summary = "회원가입",
            description = """
                    이메일 인증이 완료된 사용자를 가입 처리합니다.
                    - 사전에 이메일 인증(/verify-code)을 완료해야 합니다. 미완료 시 403.
                    - 이메일/닉네임 중복 시 409를 반환합니다.
                    - 음식 선호 카테고리(food_categories)도 함께 저장됩니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 완료", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "입력값 형식 오류 / 학교 이메일 아님 (INVALID_REQUEST, AUTH_400_EMAIL)"),
            @ApiResponse(responseCode = "403", description = "이메일 인증 미완료 (AUTH_403)"),
            @ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복 (USER_409_EMAIL, USER_409_NICK)")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        RegisterResponseDto response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "로그인",
            description = """
                    이메일/비밀번호로 로그인하고 토큰을 발급합니다.
                    - 응답으로 access_token, refresh_token, 만료시간(expires_in, 초), 닉네임, user_id를 반환합니다.
                    - 이후 인증이 필요한 API는 `Authorization: Bearer {access_token}` 헤더로 호출합니다.
                    - 이메일 미인증 계정은 403, 자격 증명 불일치는 401을 반환합니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 (토큰 발급)", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치 (AUTH_401)"),
            @ApiResponse(responseCode = "403", description = "이메일 인증 미완료 (AUTH_403)")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "닉네임 중복 확인",
            description = """
                    닉네임 사용 가능 여부를 확인합니다.
                    - 응답 body의 available(true/false)와 안내 메시지로 결과를 반환합니다(중복이어도 200).
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 완료 (available: 사용 가능 여부)", useReturnTypeSchema = true)
    })
    @GetMapping("/check-nickname")
    public ResponseEntity<CheckNicknameResponseDto> checkNickname(
            @Parameter(description = "중복 확인할 닉네임", example = "스킹이")
            @RequestParam String nickname
    ) {
        CheckNicknameResponseDto response = authService.checkNickname(nickname);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "토큰 재발급",
            description = """
                    유효한 Refresh Token으로 새 Access Token과 Refresh Token을 재발급합니다(토큰 회전).
                    - Access Token이 만료되었을 때 호출합니다.
                    - Refresh Token이 유효하지 않거나(401) 만료된 경우(401, 재로그인 필요) 에러를 반환합니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공 (새 토큰 발급)", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token (AUTH_401_REFRESH, AUTH_401_REFRESH_EXP)")
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        LoginResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "로그아웃",
            description = """
                    전달한 Refresh Token을 삭제하여 로그아웃 처리합니다.
                    - 클라이언트는 저장 중인 access/refresh 토큰을 함께 폐기해야 합니다.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 완료"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token (AUTH_401_REFRESH)")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }
}
