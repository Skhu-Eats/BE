package com.skhueats.user.service;

import com.skhueats.auth.dto.request.RegisterRequestDto;
import com.skhueats.auth.service.RedisVerificationService;
import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;
import com.skhueats.user.entity.User;
import com.skhueats.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final java.util.List<String> SCHOOL_DOMAINS = java.util.List.of(
            "@skhu.ac.kr",
            "@office.skhu.ac.kr"
    );

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

    public void register(RegisterRequestDto request) {
        if (request == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "회원가입 요청 정보가 없습니다.");
        }

        String email = request.getEmail();

        if (SCHOOL_DOMAINS.stream().noneMatch(email::endsWith)) {
            throw new ApiException(ErrorCode.INVALID_SCHOOL_EMAIL);
        }

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new ApiException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        if (!redisVerificationService.isEmailVerified(email)) {
            throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setDepartment(request.getDepartment());
        user.setAdmissionYear(request.getAdmissionYear());
        user.setBio(request.getBio());
        user.setEmailVerified(true);
        user.setMannerScore(0);
        user.setPostCount(0);
        user.setJoinCount(0);

        userRepository.save(user);
        redisVerificationService.consumeVerifiedEmail(email);
    }
}