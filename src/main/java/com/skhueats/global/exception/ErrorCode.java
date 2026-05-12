package com.skhueats.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH_403", "이메일 인증을 먼저 완료해주세요."),
    INVALID_SCHOOL_EMAIL(HttpStatus.BAD_REQUEST, "AUTH_400_EMAIL", "성공회대학교 이메일(@skhu.ac.kr 또는 @office.skhu.ac.kr)만 사용할 수 있습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_409_EMAIL", "이미 가입된 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_409_NICK", "이미 사용 중인 닉네임입니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_400_EXPIRED", "인증코드가 만료되었거나 존재하지 않습니다."),
    VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_400_MISMATCH", "인증코드가 일치하지 않습니다."),
    VERIFICATION_ATTEMPT_LOCKED(HttpStatus.TOO_MANY_REQUESTS, "AUTH_429_LOCK", "인증 실패 횟수를 초과했습니다. 10분 후 다시 시도해주세요."),
    VERIFICATION_RESEND_BLOCKED(HttpStatus.TOO_MANY_REQUESTS, "AUTH_429_RESEND", "인증 메일은 1분 후 다시 요청할 수 있습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_401", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_EXPIRED", "만료된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_REFRESH", "유효하지 않은 Refresh Token입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_REFRESH_EXP", "만료된 Refresh Token입니다. 다시 로그인해주세요."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "사용자를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
