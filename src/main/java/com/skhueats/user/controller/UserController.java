package com.skhueats.user.controller;

import com.skhueats.user.dto.request.UpdateMyProfileRequestDto;
import com.skhueats.user.dto.response.MyProfileResponseDto;
import com.skhueats.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<MyProfileResponseDto> getMyProfile() {
        MyProfileResponseDto responseDto = userService.getMyProfile();
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/me")
    public ResponseEntity<MyProfileResponseDto> updateMyProfile(
            @Valid @RequestBody UpdateMyProfileRequestDto requestDto
    ) {
        MyProfileResponseDto responseDto = userService.updateMyProfile(requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
