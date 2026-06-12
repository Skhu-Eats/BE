package com.skhueats.user.controller;

import com.skhueats.post.dto.response.PostListResponseDto;
import com.skhueats.post.service.PostService;
import com.skhueats.user.dto.request.UpdateMyProfileRequestDto;
import com.skhueats.user.dto.response.MyProfileResponseDto;
import com.skhueats.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;

    @GetMapping("/me")
    public ResponseEntity<MyProfileResponseDto> getMyProfile() {
        MyProfileResponseDto responseDto = userService.getMyProfile();
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/me/posts")
    public ResponseEntity<List<PostListResponseDto>> getMyPosts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "role", required = false) String role
    ) {
        String email = userDetails.getUsername();

        List<PostListResponseDto> response = postService.getMyPosts(email, role);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<MyProfileResponseDto> updateMyProfile(
            @Valid @RequestBody UpdateMyProfileRequestDto requestDto
    ) {
        MyProfileResponseDto responseDto = userService.updateMyProfile(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount() {
        userService.deleteAccount();
        return ResponseEntity.noContent().build();
    }
}
