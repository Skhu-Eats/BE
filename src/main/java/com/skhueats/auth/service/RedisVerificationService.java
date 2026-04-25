package com.skhueats.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisVerificationService {

    private static final long CODE_TTL_MINUTES = 5;

    private final StringRedisTemplate stringRedisTemplate;

    public RedisVerificationService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void saveVerificationCode(String email, String code) {
        String key = "email-code:" + email;
        stringRedisTemplate.opsForValue().set(key, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);
    }

    public String getVerificationCode(String email) {
        String key = "email-code:" + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    public boolean exists(String email) {
        String key = "email-code:" + email;
        Boolean result = stringRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(result);
    }

    public void deleteVerificationCode(String email) {
        String key = "email-code:" + email;
        stringRedisTemplate.delete(key);
    }

    public void saveVerifiedEmail(String email) {
        String key = "verified-email:" + email;
        stringRedisTemplate.opsForValue().set(key, "true", 30, TimeUnit.MINUTES); // 인증 후 30분 안에 회원가입
    }

    public boolean isEmailVerified(String email) {
        String key = "verified-email:" + email;
        Boolean result = stringRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(result);
    }
}