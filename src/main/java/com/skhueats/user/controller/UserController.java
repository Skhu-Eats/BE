package com.skhueats.user.controller;

import com.skhueats.user.dto.SignupRequest;
import com.skhueats.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public String signup(@RequestBody SignupRequest request) {
        userService.signup(request);
        return "회원가입 완료";
    }
}