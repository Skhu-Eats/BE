package com.skhueats.user.controller;

import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;
import com.skhueats.post.dto.response.PostListResponseDto;
import com.skhueats.post.dto.response.ParticipationHistoryPageResponseDto;
import com.skhueats.post.service.PostService;
import com.skhueats.user.dto.request.UpdateMyProfileRequestDto;
import com.skhueats.user.dto.response.MyPageResponseDto;
import com.skhueats.user.dto.response.MyProfileResponseDto;
import com.skhueats.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(
        name = "사용자 (Users)",
        description = "내 프로필, 마이페이지, 내가 쓴 글, 참여 이력, 프로필 수정, 회원 탈퇴. "
                + "모든 API는 로그인(Access Token) 후 호출해야 합니다."
)
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final PostService postService;

    public UserController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    @Operation(
            summary = "내 프로필 조회",
            description = "로그인한 사용자의 기본 프로필(닉네임, 학과, 입학연도, 매너점수, 음식 선호 등)을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)"),
            @ApiResponse(responseCode = "404", description = "사용자 없음 (USER_404)")
    })
    @GetMapping("/me")
    public ResponseEntity<MyProfileResponseDto> getMyProfile() {
        MyProfileResponseDto responseDto = userService.getMyProfile();
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "마이페이지 조회",
            description = """
                    마이페이지 정보를 한 번에 반환합니다.
                    - 프로필, 음식 선호, 최근 참여 모임 미리보기를 포함합니다.
                    - history_limit으로 최근 이력 개수를 조절합니다(1~20, 기본 5).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "history_limit 범위 오류 (INVALID_REQUEST)"),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)"),
            @ApiResponse(responseCode = "404", description = "사용자 없음 (USER_404)")
    })
    @GetMapping("/me/mypage")
    public ResponseEntity<MyPageResponseDto> getMyPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "최근 참여 이력 개수 (1~20, 기본 5)", example = "5")
            @RequestParam(name = "history_limit", defaultValue = "5") String historyLimit
    ) {
        String email = userDetails.getUsername();

        MyPageResponseDto response = userService.getMyPage(
                email,
                parsePositiveInteger(historyLimit, "history_limit")
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내가 작성한 모집글 조회",
            description = "로그인한 사용자가 호스트로 작성한 모집글 목록을 반환합니다. (GET /posts/myposts 와 동일)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)")
    })
    @GetMapping("/me/posts")
    public ResponseEntity<List<PostListResponseDto>> getMyPosts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();

        List<PostListResponseDto> response = postService.getMyPosts(email);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내 참여 이력 조회 (페이지네이션)",
            description = """
                    내가 참여한 모임 이력을 페이지로 조회합니다.
                    - 현재 참여 중(JOINED)인 모임만 반환합니다. 참여 취소(CANCELLED) 이력은 제외됩니다.
                    - page는 1부터 시작합니다(기본 1, limit 기본 20).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "page/limit 값 오류 (INVALID_REQUEST)"),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)"),
            @ApiResponse(responseCode = "404", description = "사용자 없음 (USER_404)")
    })
    @GetMapping("/me/history")
    public ResponseEntity<ParticipationHistoryPageResponseDto> getMyHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "페이지 번호 (1부터)", example = "1")
            @RequestParam(name = "page", defaultValue = "1") String page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(name = "limit", defaultValue = "20") String limit
    ) {
        String email = userDetails.getUsername();

        ParticipationHistoryPageResponseDto response = postService.getMyHistory(
                email,
                parsePositiveInteger(page, "page"),
                parsePositiveInteger(limit, "limit")
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내 프로필 수정",
            description = """
                    프로필(닉네임)을 수정합니다.
                    - 닉네임을 변경하는 경우에만 중복 검사를 수행하며, 중복 시 409를 반환합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 완료", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "입력값 형식 오류 (INVALID_REQUEST)"),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)"),
            @ApiResponse(responseCode = "409", description = "닉네임 중복 (USER_409_NICK)")
    })
    @PatchMapping("/me")
    public ResponseEntity<MyProfileResponseDto> updateMyProfile(
            @Valid @RequestBody UpdateMyProfileRequestDto requestDto
    ) {
        MyProfileResponseDto responseDto = userService.updateMyProfile(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "회원 탈퇴",
            description = """
                    로그인한 사용자의 계정을 삭제합니다.
                    - Refresh Token, 음식 선호 등 연관 데이터가 함께 정리됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 완료"),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)"),
            @ApiResponse(responseCode = "404", description = "사용자 없음 (USER_404)")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount() {
        userService.deleteAccount();
        return ResponseEntity.noContent().build();
    }

    private int parsePositiveInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, fieldName + "는 숫자여야 합니다.");
        }
    }
}
