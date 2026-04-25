package com.skhueats.auth.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AuthService {

    private static final String SCHOOL_DOMAIN = "@office.skhu.ac.kr";

    private final RedisVerificationService redisVerificationService;
    private final MailService mailService;

    public AuthService(RedisVerificationService redisVerificationService,
                       MailService mailService) {
        this.redisVerificationService = redisVerificationService;
        this.mailService = mailService;
    }

    public void validateSchoolEmail(String email) {
        if (email == null || !email.endsWith(SCHOOL_DOMAIN)) {
            throw new RuntimeException("학교 이메일만 사용할 수 있습니다.");
        }
    }

    public String createVerificationCode() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }

    public void sendVerificationCode(String email) {
        validateSchoolEmail(email);

        String code = createVerificationCode();

        redisVerificationService.saveVerificationCode(email, code);

        String subject = "성공회대학교 이메일 인증코드";
        String text = "인증코드는 [" + code + "] 입니다. 5분 이내에 입력해주세요.";

        mailService.sendEmail(email, subject, text);
    }

    public void verifyCode(String email, String code) {
        validateSchoolEmail(email);

        if (!redisVerificationService.exists(email)) {
            throw new RuntimeException("인증코드가 만료되었거나 존재하지 않습니다.");
        }

        String savedCode = redisVerificationService.getVerificationCode(email);

        if (savedCode == null || !savedCode.equals(code)) {
            throw new RuntimeException("인증코드가 일치하지 않습니다.");
        }

        redisVerificationService.deleteVerificationCode(email);

        // 이메일 인증 완료 상태 저장
        redisVerificationService.saveVerifiedEmail(email);
    }
}