package com.skhueats.auth.service;

import com.skhueats.auth.dto.request.RegisterRequestDto;
import com.skhueats.auth.dto.response.CheckNicknameResponseDto;
import com.skhueats.auth.dto.response.RegisterResponseDto;
import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;
import com.skhueats.user.entity.User;
import com.skhueats.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
public class AuthService {

    private static final List<String> SCHOOL_DOMAINS = List.of(
            "@skhu.ac.kr",
            "@office.skhu.ac.kr"
    );

    private final UserRepository userRepository;
    private final RedisVerificationService redisVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AuthService(UserRepository userRepository,
                       RedisVerificationService redisVerificationService,
                       PasswordEncoder passwordEncoder,
                       MailService mailService) {
        this.userRepository = userRepository;
        this.redisVerificationService = redisVerificationService;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    public void sendVerificationCode(String email) {
        String normalizedEmail = normalizeEmail(email);

        validateSchoolEmail(normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String code = generateVerificationCode();

        redisVerificationService.saveVerificationCode(normalizedEmail, code);
        mailService.sendVerificationCode(normalizedEmail, code);
    }

    public void verifyCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);

        validateSchoolEmail(normalizedEmail);

        boolean verified = redisVerificationService.verifyCode(normalizedEmail, code);

        if (!verified) {
            throw new ApiException(ErrorCode.VERIFICATION_CODE_MISMATCH);
        }

        redisVerificationService.markEmailAsVerified(normalizedEmail);
    }

    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {
        if (request == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "회원가입 요청 정보가 없습니다.");
        }

        String email = normalizeEmail(request.getEmail());
        String nickname = normalizeNickname(request.getNickname());
        String department = normalizeDepartment(request.getDepartment());
        String bio = normalizeBio(request.getBio());

        validateSchoolEmail(email);

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new ApiException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        if (!redisVerificationService.isEmailVerified(email)) {
            throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(nickname);
        user.setDepartment(department);
        user.setAdmissionYear(request.getAdmissionYear());
        user.setBio(bio);
        user.setEmailVerified(true);
        user.setMannerScore(0);
        user.setPostCount(0);
        user.setJoinCount(0);

        User savedUser = userRepository.save(user);

        redisVerificationService.consumeVerifiedEmail(email);

        return RegisterResponseDto.from(savedUser);
    }

    public CheckNicknameResponseDto checkNickname(String nickname) {
        String normalizedNickname = normalizeNickname(nickname);

        boolean exists = userRepository.existsByNickname(normalizedNickname);

        if (exists) {
            return new CheckNicknameResponseDto(false, "이미 사용 중인 닉네임입니다.");
        }

        return new CheckNicknameResponseDto(true, "사용 가능한 닉네임입니다.");
    }

    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_SCHOOL_EMAIL);
        }

        return email.trim().toLowerCase();
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "닉네임은 필수입니다.");
        }

        return nickname.trim();
    }

    private String normalizeDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "학과는 필수입니다.");
        }

        return department.trim();
    }

    private String normalizeBio(String bio) {
        if (bio == null || bio.trim().isEmpty()) {
            return null;
        }

        return bio.trim();
    }

    private void validateSchoolEmail(String email) {
        if (SCHOOL_DOMAINS.stream().noneMatch(email::endsWith)) {
            throw new ApiException(ErrorCode.INVALID_SCHOOL_EMAIL);
        }
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}