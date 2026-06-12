package com.skhueats.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            "입력값 형식이 올바르지 않습니다."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "SERVER_500",
            "요청 처리 중 오류가 발생했습니다."
    ),

    EMAIL_NOT_VERIFIED(
            HttpStatus.FORBIDDEN,
            "AUTH_403",
            "이메일 인증을 먼저 완료해주세요."
    ),

    INVALID_SCHOOL_EMAIL(
            HttpStatus.BAD_REQUEST,
            "AUTH_400_EMAIL",
            "성공회대학교 이메일(@skhu.ac.kr 또는 @office.skhu.ac.kr)만 사용할 수 있습니다."
    ),

    EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "USER_409_EMAIL",
            "이미 가입된 이메일입니다."
    ),

    NICKNAME_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "USER_409_NICK",
            "이미 사용 중인 닉네임입니다."
    ),

    VERIFICATION_CODE_EXPIRED(
            HttpStatus.BAD_REQUEST,
            "AUTH_400_EXPIRED",
            "인증코드가 만료되었거나 존재하지 않습니다."
    ),

    VERIFICATION_CODE_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "AUTH_400_MISMATCH",
            "인증코드가 일치하지 않습니다."
    ),

    VERIFICATION_ATTEMPT_LOCKED(
            HttpStatus.TOO_MANY_REQUESTS,
            "AUTH_429_LOCK",
            "인증 실패 횟수를 초과했습니다. 10분 후 다시 시도해주세요."
    ),

    VERIFICATION_RESEND_BLOCKED(
            HttpStatus.TOO_MANY_REQUESTS,
            "AUTH_429_RESEND",
            "인증 메일은 1분 후 다시 요청할 수 있습니다."
    ),

    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "AUTH_401",
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),

    ACCESS_TOKEN_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTH_401_TOKEN_REQUIRED",
            "Access Token이 필요합니다."
    ),

    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_401_INVALID_TOKEN",
            "유효하지 않은 Access Token입니다."
    ),

    EXPIRED_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_401_EXPIRED_TOKEN",
            "만료된 Access Token입니다."
    ),

    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_401_REFRESH",
            "유효하지 않은 Refresh Token입니다."
    ),

    EXPIRED_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_401_REFRESH_EXP",
            "만료된 Refresh Token입니다. 다시 로그인해주세요."
    ),

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER_404",
            "사용자를 찾을 수 없습니다."
    ),

    NOTIFICATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "NOTIFICATION_404",
            "알림을 찾을 수 없습니다."
    ),

    POST_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "POST_404",
            "모집글을 찾을 수 없습니다."
    ),

    POST_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "POST_403_FORBIDDEN",
            "모집글에 대한 권한이 없습니다."
    ),

    POST_DAILY_LIMIT_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS,
            "POST_429_DAILY_LIMIT",
            "하루 최대 3개까지 모집글을 작성할 수 있습니다."
    ),

    POST_SELF_JOIN_NOT_ALLOWED(
            HttpStatus.BAD_REQUEST,
            "POST_400_SELF_JOIN",
            "본인이 만든 모임에는 참여할 수 없습니다."
    ),

    POST_ALREADY_JOINED(
            HttpStatus.BAD_REQUEST,
            "POST_400_ALREADY_JOINED",
            "이미 참여 중인 모임입니다."
    ),

    POST_RECRUITMENT_CLOSED(
            HttpStatus.CONFLICT,
            "POST_409_CLOSED",
            "모집이 마감된 모임입니다."
    ),

    POST_FULL(
            HttpStatus.CONFLICT,
            "POST_409_FULL",
            "모집 인원이 가득 찼습니다."
    );

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
