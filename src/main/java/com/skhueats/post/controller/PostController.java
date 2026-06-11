package com.skhueats.post.controller;

import com.skhueats.post.dto.request.CreatePostRequestDto;
import com.skhueats.post.dto.request.UpdatePostRequestDto;
import com.skhueats.post.dto.response.CreatePostResponseDto;
import com.skhueats.post.dto.response.PostDetailResponseDto;
import com.skhueats.post.dto.response.PostListResponseDto;
import com.skhueats.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<PostListResponseDto>> getPosts(
            @RequestParam(name = "time_slot", required = false) String timeSlot,
            @RequestParam(name = "status", required = false) String status
    ) {
        List<PostListResponseDto> posts = postService.getPosts(timeSlot, status);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponseDto> getPost(@PathVariable("postId") String postId) {
        PostDetailResponseDto response = postService.getPost(postId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<PostDetailResponseDto> updatePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("postId") String postId,
            @Valid @RequestBody UpdatePostRequestDto requestDto
    ) {
        String email = userDetails.getUsername();

        PostDetailResponseDto response = postService.updatePost(email, postId, requestDto);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("postId") String postId
    ) {
        String email = userDetails.getUsername();

        postService.deletePost(email, postId);

        return ResponseEntity.noContent().build();
    }
}
