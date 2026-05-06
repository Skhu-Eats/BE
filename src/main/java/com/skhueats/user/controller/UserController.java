package com.skhueats.user.controller;

import com.skhueats.auth.dto.request.RegisterRequestDto;
import com.skhueats.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDto request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 완료");
    }
}