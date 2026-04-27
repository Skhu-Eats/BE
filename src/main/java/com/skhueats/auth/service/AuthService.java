package com.skhueats.auth.service;

import com.skhueats.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AuthService {

    private static final String SCHOOL_DOMAIN = "@office.skhu.ac.kr";

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
            throw new RuntimeException("학교 이메일만 사용할 수 있습니다.");
        }
    }

    public void validateDuplicateUser(String email, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
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
            throw new RuntimeException("인증 메일은 1분 후 다시 요청할 수 있습니다.");
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
            throw new RuntimeException("인증 실패 횟수를 초과했습니다. 10분 후 다시 시도해주세요.");
        }

        if (!redisVerificationService.exists(email)) {
            throw new RuntimeException("인증코드가 만료되었거나 존재하지 않습니다.");
        }

        String savedCode = redisVerificationService.getVerificationCode(email);

        if (savedCode == null || !savedCode.equals(code)) {
            int failedCount = redisVerificationService.increaseFailCount(email);

            if (failedCount >= 5) {
                throw new RuntimeException("인증코드를 5회 이상 틀렸습니다. 10분 동안 인증이 제한됩니다.");
            }

            throw new RuntimeException("인증코드가 일치하지 않습니다. 현재 실패 횟수: " + failedCount + "회");
        }

        redisVerificationService.saveVerifiedEmail(email);
        redisVerificationService.clearVerificationState(email);
    }
}