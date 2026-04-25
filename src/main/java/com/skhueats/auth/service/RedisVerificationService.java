package com.skhueats.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisVerificationService {

    // 인증코드 유효시간: 5분
    // 사용자가 인증코드를 받은 뒤 5분 안에 입력해야 함
    private static final long CODE_TTL_MINUTES = 5;

    // 이메일 인증 완료 상태 유지 시간: 30분
    // 인증 성공 후 30분 안에 회원가입을 완료해야 함
    private static final long VERIFIED_EMAIL_TTL_MINUTES = 30;

    // 인증 메일 재요청 제한 시간: 60초
    // 같은 이메일로 1분 안에 인증 메일을 다시 요청하지 못하게 함
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    // 인증 실패 잠금 시간: 10분
    // 인증코드를 여러 번 틀리면 10분 동안 인증 시도 차단
    private static final long LOCK_TTL_MINUTES = 10;

    // 최대 인증 실패 횟수
    // 5번 틀리면 잠금 처리
    private static final int MAX_FAILED_COUNT = 5;

    private final StringRedisTemplate stringRedisTemplate;

    public RedisVerificationService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 인증코드를 저장하는 Redis key
    // 예: email-code:test@skhu.ac.kr
    private String codeKey(String email) {
        return "email-code:" + email;
    }

    // 인증 완료 이메일을 저장하는 Redis key
    // 예: verified-email:test@skhu.ac.kr
    private String verifiedKey(String email) {
        return "verified-email:" + email;
    }

    // 인증 메일 재요청 제한을 위한 Redis key
    // 이 key가 존재하면 아직 재요청 제한 시간이 지나지 않은 상태
    // 예: email-code-resend:test@skhu.ac.kr
    private String resendKey(String email) {
        return "email-code-resend:" + email;
    }

    // 인증 실패 횟수를 저장하는 Redis key
    // 예: email-code-fail:test@skhu.ac.kr
    private String failKey(String email) {
        return "email-code-fail:" + email;
    }

    // 인증 시도 잠금 상태를 저장하는 Redis key
    // 이 key가 존재하면 10분 동안 인증 시도 불가
    // 예: email-code-lock:test@skhu.ac.kr
    private String lockKey(String email) {
        return "email-code-lock:" + email;
    }

    /**
     * 인증코드를 Redis에 저장한다.
     *
     * 저장되는 값:
     * 1. 인증코드
     * 2. 재요청 제한 key
     *
     * 함께 초기화되는 값:
     * 1. 기존 실패 횟수
     * 2. 기존 잠금 상태
     *
     * 이유:
     * 새 인증코드를 발급하면 이전 인증 실패 기록은 의미가 없어지기 때문이다.
     */
    public void saveVerificationCode(String email, String code) {
        // 인증코드 저장
        // CODE_TTL_MINUTES가 지나면 Redis에서 자동 삭제됨
        stringRedisTemplate.opsForValue()
                .set(codeKey(email), code, CODE_TTL_MINUTES, TimeUnit.MINUTES);

        // 재요청 제한 key 저장
        // 이 key가 존재하는 동안은 인증 메일 재요청을 막음
        stringRedisTemplate.opsForValue()
                .set(resendKey(email), "true", RESEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);

        // 새 코드를 발급했으므로 기존 실패 횟수 초기화
        stringRedisTemplate.delete(failKey(email));

        // 새 코드를 발급했으므로 기존 잠금 상태 초기화
        stringRedisTemplate.delete(lockKey(email));
    }

    /**
     * Redis에서 이메일에 해당하는 인증코드를 조회한다.
     *
     * 반환값:
     * - 인증코드가 있으면 코드 문자열 반환
     * - 만료되었거나 존재하지 않으면 null 반환
     */
    public String getVerificationCode(String email) {
        return stringRedisTemplate.opsForValue().get(codeKey(email));
    }

    /**
     * 해당 이메일의 인증코드가 Redis에 존재하는지 확인한다.
     *
     * true:
     * - 인증코드가 아직 만료되지 않음
     *
     * false:
     * - 인증코드가 없음
     * - 또는 인증코드가 만료되어 Redis에서 삭제됨
     */
    public boolean exists(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(codeKey(email)));
    }

    /**
     * 인증코드를 삭제한다.
     *
     * 보통 인증 성공 후 더 이상 같은 코드를 사용할 수 없게 하기 위해 사용한다.
     */
    public void deleteVerificationCode(String email) {
        stringRedisTemplate.delete(codeKey(email));
    }

    /**
     * 이메일 인증 완료 상태를 저장한다.
     *
     * 인증 완료 후 바로 회원가입하지 않을 수 있으므로
     * 30분 동안만 인증 완료 상태를 유지한다.
     *
     * register 로직에서는 이 값을 확인해서
     * 이메일 인증을 완료한 사용자만 회원가입할 수 있게 하면 된다.
     */
    public void saveVerifiedEmail(String email) {
        stringRedisTemplate.opsForValue()
                .set(verifiedKey(email), "true", VERIFIED_EMAIL_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 해당 이메일이 인증 완료 상태인지 확인한다.
     *
     * true:
     * - verify-code 성공 이력이 있음
     * - 인증 완료 후 30분이 지나지 않음
     *
     * false:
     * - 인증한 적 없음
     * - 또는 인증 완료 상태가 만료됨
     */
    public boolean isEmailVerified(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(verifiedKey(email)));
    }

    /**
     * 인증 메일 재요청 제한 여부를 확인한다.
     *
     * true:
     * - 마지막 인증 메일 발송 후 60초가 지나지 않음
     *
     * false:
     * - 재요청 가능
     */
    public boolean isResendBlocked(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(resendKey(email)));
    }

    /**
     * 인증 시도가 잠긴 상태인지 확인한다.
     *
     * true:
     * - 인증코드를 5회 이상 틀려서 10분 잠금 상태
     *
     * false:
     * - 인증 시도 가능
     */
    public boolean isLocked(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey(email)));
    }

    /**
     * 인증코드 실패 횟수를 1 증가시킨다.
     *
     * 동작:
     * 1. failKey 값을 1 증가시킴
     * 2. 처음 실패한 경우 TTL을 인증코드 만료 시간과 동일하게 설정
     * 3. 실패 횟수가 MAX_FAILED_COUNT 이상이면 잠금 처리
     *
     * 반환값:
     * - 현재 실패 횟수
     */
    public int increaseFailCount(String email) {
        Long count = stringRedisTemplate.opsForValue().increment(failKey(email));

        // 처음 실패한 경우
        // 실패 횟수 기록도 인증코드와 같이 5분 후 만료되도록 설정
        if (count != null && count == 1) {
            stringRedisTemplate.expire(failKey(email), CODE_TTL_MINUTES, TimeUnit.MINUTES);
        }

        // 실패 횟수가 5회 이상이면 인증 시도 잠금
        if (count != null && count >= MAX_FAILED_COUNT) {
            lock(email);
        }

        return count == null ? 0 : count.intValue();
    }

    /**
     * 이메일 인증 시도를 잠금 처리한다.
     *
     * lockKey를 Redis에 저장하고,
     * LOCK_TTL_MINUTES가 지나면 자동으로 잠금 해제된다.
     */
    private void lock(String email) {
        stringRedisTemplate.opsForValue()
                .set(lockKey(email), "true", LOCK_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 인증 관련 임시 상태를 모두 삭제한다.
     *
     * 삭제 대상:
     * - 인증코드
     * - 실패 횟수
     * - 잠금 상태
     * - 재요청 제한
     *
     * 주의:
     * verified-email key는 삭제하지 않는다.
     * 인증 성공 후 register에서 인증 완료 여부를 확인해야 하기 때문이다.
     */
    public void clearVerificationState(String email) {
        stringRedisTemplate.delete(codeKey(email));
        stringRedisTemplate.delete(failKey(email));
        stringRedisTemplate.delete(lockKey(email));
        stringRedisTemplate.delete(resendKey(email));
    }
}