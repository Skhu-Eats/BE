package com.skhueats.auth.service;

import com.skhueats.auth.repository.EmailVerificationRepository;
import org.springframework.stereotype.Service;

@Service
public class RedisVerificationService {

    private static final long CODE_TTL_MINUTES = 5;
    private static final long VERIFIED_EMAIL_TTL_MINUTES = 30;
    private static final long RESEND_COOLDOWN_SECONDS = 60;
    private static final long LOCK_TTL_MINUTES = 10;
    private static final int MAX_FAILED_COUNT = 5;

    private final EmailVerificationRepository emailVerificationRepository;

    public RedisVerificationService(EmailVerificationRepository emailVerificationRepository) {
        this.emailVerificationRepository = emailVerificationRepository;
    }

    /**
     * 이메일 인증 코드 저장
     */
    public void saveVerificationCode(String email, String code) {
        emailVerificationRepository.saveCode(email, code, CODE_TTL_MINUTES);
        emailVerificationRepository.saveResendCooldown(email, RESEND_COOLDOWN_SECONDS);
        emailVerificationRepository.deleteFailCount(email);
        emailVerificationRepository.deleteLock(email);
    }

    /**
     * 이메일 인증 코드 조회
     */
    public String getVerificationCode(String email) {
        return emailVerificationRepository.getCode(email);
    }

    /**
     * 이메일 인증 코드 존재 여부 확인
     */
    public boolean exists(String email) {
        return emailVerificationRepository.hasCode(email);
    }

    /**
     * 이메일 인증 성공 상태 저장
     *
     * verify-code 성공 시 호출됨.
     * Redis에 verified-email:{email} 키를 저장한다.
     */
    public void saveVerifiedEmail(String email) {
        emailVerificationRepository.saveVerifiedEmail(email, VERIFIED_EMAIL_TTL_MINUTES);
    }

    /**
     * 이메일 인증 완료 여부 확인
     *
     * 단순 확인용.
     * Redis 키를 삭제하지 않는다.
     */
    public boolean isEmailVerified(String email) {
        return emailVerificationRepository.hasVerifiedEmail(email);
    }

    /**
     * 이메일 인증 완료 여부 확인 후 삭제
     *
     * 회원가입 시 호출하는 메서드.
     * 인증 완료 상태를 1회만 사용하기 위해 확인과 동시에 Redis 키를 삭제한다.
     */
    public boolean verifyAndConsumeEmail(String email) {
        return emailVerificationRepository.consumeVerifiedEmail(email);
    }

    /**
     * 기존 이름 유지용 메서드
     *
     * 이미 다른 코드에서 consumeVerifiedEmail()을 쓰고 있을 수 있으므로 남겨둔다.
     */
    public boolean consumeVerifiedEmail(String email) {
        return verifyAndConsumeEmail(email);
    }

    /**
     * 인증 코드 재전송 제한 여부 확인
     */
    public boolean isResendBlocked(String email) {
        return emailVerificationRepository.isResendBlocked(email);
    }

    /**
     * 인증 시도 잠금 여부 확인
     */
    public boolean isLocked(String email) {
        return emailVerificationRepository.isLocked(email);
    }

    /**
     * 인증 실패 횟수 증가
     *
     * 실패 횟수가 MAX_FAILED_COUNT 이상이면 이메일 인증 시도를 잠근다.
     */
    public int increaseFailCount(String email) {
        int failedCount = emailVerificationRepository.incrementFailCount(email, CODE_TTL_MINUTES);

        if (failedCount >= MAX_FAILED_COUNT) {
            emailVerificationRepository.saveLock(email, LOCK_TTL_MINUTES);
        }

        return failedCount;
    }

    /**
     * 인증 코드 관련 상태 초기화
     *
     * 인증 성공 후 인증 코드, 실패 횟수, 잠금, 재전송 제한 정보를 삭제한다.
     * 단, verified-email 키는 삭제하지 않는다.
     */
    public void clearVerificationState(String email) {
        emailVerificationRepository.deleteCode(email);
        emailVerificationRepository.deleteFailCount(email);
        emailVerificationRepository.deleteLock(email);
        emailVerificationRepository.deleteResendCooldown(email);
    }
}