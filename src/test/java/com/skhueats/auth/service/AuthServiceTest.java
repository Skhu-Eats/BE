package com.skhueats.auth.service;

import com.skhueats.auth.dto.request.LoginRequest;
import com.skhueats.auth.dto.request.LogoutRequest;
import com.skhueats.auth.dto.request.TokenRefreshRequest;
import com.skhueats.auth.dto.response.LoginResponse;
import com.skhueats.auth.entity.RefreshToken;
import com.skhueats.auth.jwt.JwtTokenProvider;
import com.skhueats.auth.repository.RefreshTokenRepository;
import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;
import com.skhueats.user.entity.User;
import com.skhueats.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private RedisVerificationService redisVerificationService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MailService mailService;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpiry", 1800000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiry", 604800000L);
    }

    private User createVerifiedUser() {
        User user = new User();
        user.setEmail("test@skhu.ac.kr");
        user.setPasswordHash("hashedPassword");
        user.setNickname("테스트유저");
        user.setEmailVerified(true);
        user.setDepartment("컴퓨터공학과");
        user.setAdmissionYear(2024);
        user.setMannerScore(0);
        user.setPostCount(0);
        user.setJoinCount(0);
        return user;
    }

    // ── 로그인 ──────────────────────────────────────────

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        User user = createVerifiedUser();
        given(userRepository.findByEmail("test@skhu.ac.kr")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("test1234", "hashedPassword")).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(anyString())).willReturn("accessToken");
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("refreshToken");
        given(refreshTokenRepository.findByEmail(anyString())).willReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        ReflectionTestUtils.setField(req, "email", "test@skhu.ac.kr");
        ReflectionTestUtils.setField(req, "password", "test1234");

        LoginResponse res = authService.login(req);

        assertThat(res.getAccessToken()).isEqualTo("accessToken");
        assertThat(res.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(res.getNickname()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 실패")
    void login_fail_email_not_found() {
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        ReflectionTestUtils.setField(req, "email", "notexist@skhu.ac.kr");
        ReflectionTestUtils.setField(req, "password", "test1234");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("이메일 미인증 사용자 로그인 시 실패")
    void login_fail_email_not_verified() {
        User user = createVerifiedUser();
        user.setEmailVerified(false);
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        ReflectionTestUtils.setField(req, "email", "test@skhu.ac.kr");
        ReflectionTestUtils.setField(req, "password", "test1234");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getErrorCode())
                        .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED));
    }

    @Test
    @DisplayName("비밀번호 불일치 시 로그인 실패")
    void login_fail_wrong_password() {
        User user = createVerifiedUser();
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        LoginRequest req = new LoginRequest();
        ReflectionTestUtils.setField(req, "email", "test@skhu.ac.kr");
        ReflectionTestUtils.setField(req, "password", "wrongpassword");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    // ── Refresh Token ──────────────────────────────────

    @Test
    @DisplayName("Refresh Token 재발급 성공")
    void refresh_success() {
        User user = createVerifiedUser();
        RefreshToken refreshToken = RefreshToken.builder()
                .email("test@skhu.ac.kr")
                .token("validRefreshToken")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        given(refreshTokenRepository.findByToken("validRefreshToken"))
                .willReturn(Optional.of(refreshToken));
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(jwtTokenProvider.generateAccessToken(anyString())).willReturn("newAccessToken");
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("newRefreshToken");

        TokenRefreshRequest req = new TokenRefreshRequest();
        ReflectionTestUtils.setField(req, "refreshToken", "validRefreshToken");

        LoginResponse res = authService.refresh(req);

        assertThat(res.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(res.getRefreshToken()).isEqualTo("newRefreshToken");
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token으로 재발급 시 실패")
    void refresh_fail_invalid_token() {
        given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.empty());

        TokenRefreshRequest req = new TokenRefreshRequest();
        ReflectionTestUtils.setField(req, "refreshToken", "invalidToken");

        assertThatThrownBy(() -> authService.refresh(req))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    // ── 로그아웃 ───────────────────────────────────────

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {
        RefreshToken refreshToken = RefreshToken.builder()
                .email("test@skhu.ac.kr")
                .token("validRefreshToken")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        given(refreshTokenRepository.findByToken("validRefreshToken"))
                .willReturn(Optional.of(refreshToken));

        LogoutRequest req = new LogoutRequest();
        ReflectionTestUtils.setField(req, "refreshToken", "validRefreshToken");

        authService.logout(req);

        verify(refreshTokenRepository).delete(refreshToken);
    }
}

