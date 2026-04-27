package com.skhueats.user.repository;

import com.skhueats.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email); // 로그인용

    boolean existsByEmail(String email); // 회원가입 중복 체크

    boolean existsByNickname(String nickname); // 추가 (필수)
}