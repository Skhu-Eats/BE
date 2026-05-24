package com.skhueats.user.controller;

import com.skhueats.user.dto.response.MyProfileResponseDto;
import com.skhueats.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<MyProfileResponseDto> getMyProfile() {
        MyProfileResponseDto response = userService.getMyProfile();

        return ResponseEntity.ok(response);
    }
}