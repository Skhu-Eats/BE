package com.skhueats.user.service;

import com.skhueats.auth.service.RedisVerificationService;
import com.skhueats.user.dto.SignupRequest;
import com.skhueats.user.entity.User;
import com.skhueats.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RedisVerificationService redisVerificationService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RedisVerificationService redisVerificationService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.redisVerificationService = redisVerificationService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 회원가입 처리 메서드
     *
     * 처리 순서:
     * 1. 필수 입력값 검증
     * 2. 이메일 인증 완료 여부 확인
     * 3. 이메일 중복 가입 여부 확인
     * 4. 비밀번호 암호화
     * 5. User 엔티티 생성 및 저장
     * 6. 회원가입 완료 후 Redis 인증 상태 삭제
     */
    public void signup(SignupRequest request) {

        // 요청 객체 자체가 null이면 이후 getEmail() 등에서 NullPointerException 발생 가능
        if (request == null) {
            throw new RuntimeException("회원가입 요청 정보가 없습니다.");
        }

        String email = request.getEmail();
        String password = request.getPassword();
        String nickname = request.getNickname();

        // 필수값 검증
        // email, password, nickname은 회원가입에 반드시 필요
        if (email == null || email.isBlank()) {
            throw new RuntimeException("이메일은 필수입니다.");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("비밀번호는 필수입니다.");
        }

        if (nickname == null || nickname.isBlank()) {
            throw new RuntimeException("닉네임은 필수입니다.");
        }

        // 이메일 인증 여부 확인
        // verify-code API를 성공한 이메일만 Redis에 verified-email key가 저장됨
        if (!redisVerificationService.isEmailVerified(email)) {
            throw new RuntimeException("이메일 인증을 먼저 완료해주세요.");
        }

        // 이메일 중복 가입 방지
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        // User 엔티티 생성
        User user = new User();

        // UUID 문자열을 사용자 PK로 사용
        user.setId(UUID.randomUUID().toString());

        // 사용자 이메일 저장
        user.setEmail(email);

        // PasswordEncoder를 사용해서 해시값으로 저장
        user.setPasswordHash(passwordEncoder.encode(password));

        // 닉네임 저장
        user.setNickname(nickname);

        // 이메일 인증 완료 상태 저장
        user.setEmailVerified(true);

        // DB에 사용자 저장
        userRepository.save(user);

        // 회원가입이 완료되었으므로 Redis에 남아 있는 인증 임시 상태 삭제
        // 인증 상태를 계속 남겨두면 같은 인증 정보가 재사용될 수 있음
        redisVerificationService.clearVerificationState(email);
    }
}