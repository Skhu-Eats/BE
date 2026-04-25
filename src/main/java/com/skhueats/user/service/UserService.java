package com.skhueats.user.service;

import com.skhueats.auth.service.RedisVerificationService;
import com.skhueats.user.dto.SignupRequest;
import com.skhueats.user.entity.User;
import com.skhueats.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RedisVerificationService redisVerificationService;

    public UserService(UserRepository userRepository,
                       RedisVerificationService redisVerificationService) {
        this.userRepository = userRepository;
        this.redisVerificationService = redisVerificationService;
    }

    public void signup(SignupRequest request) {
        String email = request.getEmail();

        if (!redisVerificationService.isEmailVerified(email)) {
            throw new RuntimeException("이메일 인증을 먼저 완료해주세요.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(email);
        user.setPasswordHash(request.getPassword()); // 임시
        user.setNickname(request.getNickname());
        user.setEmailVerified(true);

        userRepository.save(user);
    }
}