package com.skhueats.auth.service;

import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;
import com.skhueats.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AuthService {

    private static final String SCHOOL_DOMAIN = "@skhu.ac.kr";

    private final RedisVerificationService redisVerificationService;
    private final MailService mailService;
    private final UserRepository userRepository;

    public AuthService(RedisVerificationService redisVerificationService,
                       MailService mailService,
                       UserRepository userRepository) {
        this.redisVerificationService = redisVerificationService;
        this.mailService = mailService;
        this.userRepository = userRepository;
    }

    public void validateSchoolEmail(String email) {
        if (email == null || !email.endsWith(SCHOOL_DOMAIN)) {
            throw new ApiException(ErrorCode.INVALID_SCHOOL_EMAIL);
        }
    }


    public void validateDuplicateUser(String email, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new ApiException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

    public void validateEmailVerified(String email) {
        if (!redisVerificationService.consumeVerifiedEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
    }


    public String createVerificationCode() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }

    public void sendVerificationCode(String email) {
        validateSchoolEmail(email);

        if (redisVerificationService.isResendBlocked(email)) {
            throw new ApiException(ErrorCode.VERIFICATION_RESEND_BLOCKED);
        }

        String code = createVerificationCode();
        redisVerificationService.saveVerificationCode(email, code);

        String subject = "성공회대학교 이메일 인증코드";
        String text = "인증코드는 [" + code + "] 입니다. 5분 이내에 입력해주세요.";
        mailService.sendEmail(email, subject, text);
    }

    public void verifyCode(String email, String code) {
        validateSchoolEmail(email);

        if (redisVerificationService.isLocked(email)) {
            throw new ApiException(ErrorCode.VERIFICATION_ATTEMPT_LOCKED);
        }

        if (!redisVerificationService.exists(email)) {
            throw new ApiException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        String savedCode = redisVerificationService.getVerificationCode(email);

        if (savedCode == null || !savedCode.equals(code)) {
            int failedCount = redisVerificationService.increaseFailCount(email);
            if (failedCount >= 5) {
                throw new ApiException(ErrorCode.VERIFICATION_ATTEMPT_LOCKED);
            }
            throw new ApiException(ErrorCode.VERIFICATION_CODE_MISMATCH,
                    "인증코드가 일치하지 않습니다. 현재 실패 횟수: " + failedCount + "회");
        }

        redisVerificationService.saveVerifiedEmail(email);
        redisVerificationService.clearVerificationState(email);
    }


}