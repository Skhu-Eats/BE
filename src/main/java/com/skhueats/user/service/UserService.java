package com.skhueats.user.service;

import com.skhueats.auth.service.RedisVerificationService;
import com.skhueats.auth.dto.request.RegisterRequestDto;
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

    public void signup(RegisterRequestDto request) {

        if (request == null) {
            throw new RuntimeException("회원가입 요청 정보가 없습니다.");
        }

        String email = request.getEmail();
        String password = request.getPassword();
        String nickname = request.getNickname();
        String department = request.getDepartment();
        String admissionYear = request.getAdmissionYear();
        String bio = request.getBio();

        if (email == null || email.isBlank()) {
            throw new RuntimeException("이메일은 필수입니다.");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("비밀번호는 필수입니다.");
        }

        if (nickname == null || nickname.isBlank()) {
            throw new RuntimeException("닉네임은 필수입니다.");
        }

        if (department == null || department.isBlank()) {
            throw new RuntimeException("학과는 필수입니다.");
        }

        if (admissionYear == null || admissionYear.isBlank()) {
            throw new RuntimeException("입학연도는 필수입니다.");
        }

        if (!redisVerificationService.isEmailVerified(email)) {
            throw new RuntimeException("이메일 인증을 먼저 완료해주세요.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        User user = new User();

        user.setId(UUID.randomUUID().toString());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setDepartment(department);
        user.setAdmissionYear(admissionYear);
        user.setBio(bio);
        user.setEmailVerified(true);
        user.setMannerScore(0);
        user.setPostCount(0);
        user.setJoinCount(0);

        userRepository.save(user);

        redisVerificationService.clearVerificationState(email);
    }
}