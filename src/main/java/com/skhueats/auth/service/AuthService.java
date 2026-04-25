package com.skhueats.auth.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AuthService {

    // 성공회대학교 Office 학생 이메일 도메인
    private static final String SCHOOL_DOMAIN = "@office.skhu.ac.kr";

    private final RedisVerificationService redisVerificationService;
    private final MailService mailService;

    public AuthService(RedisVerificationService redisVerificationService,
                       MailService mailService) {
        this.redisVerificationService = redisVerificationService;
        this.mailService = mailService;
    }

    /**
     * 학교 이메일 형식인지 검증한다.
     *
     * 현재 정책:
     * - @office.skhu.ac.kr 도메인만 허용
     */
    public void validateSchoolEmail(String email) {
        if (email == null || !email.endsWith(SCHOOL_DOMAIN)) {
            throw new RuntimeException("학교 이메일만 사용할 수 있습니다.");
        }
    }

    /**
     * 6자리 인증코드를 생성한다.
     *
     * 예:
     * - 100000 ~ 999999 사이의 숫자 문자열
     */
    public String createVerificationCode() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }

    /**
     * 이메일 인증코드를 발송한다.
     *
     * 처리 순서:
     * 1. 학교 이메일 도메인 검증
     * 2. 인증 메일 재요청 제한 확인
     * 3. 인증코드 생성
     * 4. Redis에 인증코드 저장
     * 5. 이메일 발송
     *
     * Redis 저장 내용:
     * - 인증코드: 5분 유지
     * - 재요청 제한 key: 60초 유지
     */
    public void sendVerificationCode(String email) {
        validateSchoolEmail(email);

        // 같은 이메일로 1분 이내에 다시 요청하는 경우 차단
        if (redisVerificationService.isResendBlocked(email)) {
            throw new RuntimeException("인증 메일은 1분 후 다시 요청할 수 있습니다.");
        }

        String code = createVerificationCode();

        // 인증코드를 Redis에 저장
        // 저장 시 기존 실패 횟수, 잠금 상태도 초기화됨
        redisVerificationService.saveVerificationCode(email, code);

        String subject = "성공회대학교 이메일 인증코드";
        String text = "인증코드는 [" + code + "] 입니다. 5분 이내에 입력해주세요.";

        mailService.sendEmail(email, subject, text);
    }

    /**
     * 사용자가 입력한 인증코드를 검증한다.
     *
     * 처리 순서:
     * 1. 학교 이메일 도메인 검증
     * 2. 잠금 상태 확인
     * 3. 인증코드 존재 여부 확인
     * 4. Redis에 저장된 코드 조회
     * 5. 입력 코드와 저장 코드 비교
     * 6. 불일치 시 실패 횟수 증가
     * 7. 일치 시 인증 완료 상태 저장
     * 8. 인증 관련 임시 상태 삭제
     */
    public void verifyCode(String email, String code) {
        validateSchoolEmail(email);

        // 인증코드를 5회 이상 틀린 이메일은 10분 동안 인증 시도 차단
        if (redisVerificationService.isLocked(email)) {
            throw new RuntimeException("인증 실패 횟수를 초과했습니다. 10분 후 다시 시도해주세요.");
        }

        // 인증코드가 없으면 아직 발급하지 않았거나, 5분이 지나 만료된 상태
        if (!redisVerificationService.exists(email)) {
            throw new RuntimeException("인증코드가 만료되었거나 존재하지 않습니다.");
        }

        String savedCode = redisVerificationService.getVerificationCode(email);

        // 저장된 코드가 없거나 사용자가 입력한 코드와 다르면 실패 처리
        if (savedCode == null || !savedCode.equals(code)) {
            int failedCount = redisVerificationService.increaseFailCount(email);

            if (failedCount >= 5) {
                throw new RuntimeException("인증코드를 5회 이상 틀렸습니다. 10분 동안 인증이 제한됩니다.");
            }

            throw new RuntimeException("인증코드가 일치하지 않습니다. 현재 실패 횟수: " + failedCount + "회");
        }

        // 인증 성공 상태 저장
        // 회원가입 시 이 값을 확인해서 인증 완료 여부를 판단함
        redisVerificationService.saveVerifiedEmail(email);

        // 인증 성공 후에는 기존 인증코드, 실패 횟수, 잠금, 재요청 제한 상태 삭제
        // 단, verified-email 상태는 삭제하지 않음
        redisVerificationService.clearVerificationState(email);
    }
}