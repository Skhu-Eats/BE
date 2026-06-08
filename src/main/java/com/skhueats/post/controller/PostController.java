package com.skhueats.post.controller;

import com.skhueats.post.dto.request.CreatePostRequestDto;
import com.skhueats.post.dto.response.CreatePostResponseDto;
import com.skhueats.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<CreatePostResponseDto> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePostRequestDto requestDto
    ) {
        String email = userDetails.getUsername();

        CreatePostResponseDto response = postService.createPost(email, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
