package com.skhueats.auth.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class EmailVerificationRepository {

    private final StringRedisTemplate stringRedisTemplate;

    public EmailVerificationRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void saveCode(String email, String code, long ttlMinutes) {
        stringRedisTemplate.opsForValue().set(codeKey(email), code, ttlMinutes, TimeUnit.MINUTES);
    }

    public String getCode(String email) {
        return stringRedisTemplate.opsForValue().get(codeKey(email));
    }

    public boolean hasCode(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(codeKey(email)));
    }

    public void saveVerifiedEmail(String email, long ttlMinutes) {
        stringRedisTemplate.opsForValue().set(verifiedKey(email), "true", ttlMinutes, TimeUnit.MINUTES);
    }

    public boolean hasVerifiedEmail(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(verifiedKey(email)));
    }

    public boolean consumeVerifiedEmail(String email) {
        Boolean deleted = stringRedisTemplate.delete(verifiedKey(email));
        return Boolean.TRUE.equals(deleted);
    }

    public void saveResendCooldown(String email, long ttlSeconds) {
        stringRedisTemplate.opsForValue().set(resendKey(email), "true", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isResendBlocked(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(resendKey(email)));
    }

    public boolean isLocked(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey(email)));
    }

    public int incrementFailCount(String email, long ttlMinutes) {
        Long count = stringRedisTemplate.opsForValue().increment(failKey(email));
        if (count != null && count == 1) {
            stringRedisTemplate.expire(failKey(email), ttlMinutes, TimeUnit.MINUTES);
        }
        return count == null ? 0 : count.intValue();
    }

    public void saveLock(String email, long ttlMinutes) {
        stringRedisTemplate.opsForValue().set(lockKey(email), "true", ttlMinutes, TimeUnit.MINUTES);
    }

    public void deleteCode(String email) {
        stringRedisTemplate.delete(codeKey(email));
    }

    public void deleteFailCount(String email) {
        stringRedisTemplate.delete(failKey(email));
    }

    public void deleteLock(String email) {
        stringRedisTemplate.delete(lockKey(email));
    }

    public void deleteResendCooldown(String email) {
        stringRedisTemplate.delete(resendKey(email));
    }

    private String codeKey(String email) {
        return "email-code:" + email;
    }

    private String verifiedKey(String email) {
        return "verified-email:" + email;
    }

    private String resendKey(String email) {
        return "email-code-resend:" + email;
    }

    private String failKey(String email) {
        return "email-code-fail:" + email;
    }

    private String lockKey(String email) {
        return "email-code-lock:" + email;
    }
}