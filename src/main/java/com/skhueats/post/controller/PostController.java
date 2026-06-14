package com.skhueats.post.controller;

import com.skhueats.post.dto.request.CreatePostRequestDto;
import com.skhueats.post.dto.request.UpdatePostRequestDto;
import com.skhueats.post.dto.response.CreatePostResponseDto;
import com.skhueats.post.dto.response.JoinPostResponseDto;
import com.skhueats.post.dto.response.PostDetailResponseDto;
import com.skhueats.post.dto.response.PostListResponseDto;
import com.skhueats.post.entity.FoodCategory;
import com.skhueats.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "모집글 (Posts)",
        description = "밥 모임 모집글의 작성/조회/참여/취소/수정/삭제. "
                + "모든 API는 로그인(Access Token) 후 호출해야 합니다."
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Operation(
            summary = "모집글 작성",
            description = """
                    새 밥 모임 모집글을 등록합니다.
                    - 작성자는 자동으로 호스트가 되며 현재 참여 인원은 1명으로 시작합니다.
                    - 모집 인원(max_participants)은 2~4명, 음식 카테고리는 1개 이상 지정합니다.
                    - 하루 최대 3개까지만 작성할 수 있습니다(초과 시 429).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 완료", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "입력값 형식 오류 (INVALID_REQUEST)"),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)"),
            @ApiResponse(responseCode = "429", description = "하루 작성 한도 초과 (POST_429_DAILY_LIMIT)")
    })
    @PostMapping
    public ResponseEntity<CreatePostResponseDto> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePostRequestDto requestDto
    ) {
        String email = userDetails.getUsername();

        CreatePostResponseDto response = postService.createPost(email, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "모집글 목록 조회 (필터)",
            description = """
                    모집글 목록을 조건에 따라 조회합니다. 모든 필터는 선택값이며, 미지정 시 전체를 반환합니다.
                    - status 미지정 시 OPEN/CLOSED/CANCELLED 전체가 포함됩니다(풀방이어도 목록에 노출).
                    - 마감시간이 지난 글은 제외됩니다(KST 기준).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "필터 값 오류 (status/max_participants 범위 등)"),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)")
    })
    @GetMapping
    public ResponseEntity<List<PostListResponseDto>> getPosts(
            @Parameter(description = "시간대 필터 (예: LUNCH/DINNER 등 정의된 슬롯)")
            @RequestParam(name = "time_slot", required = false) String timeSlot,
            @Parameter(description = "모집 상태 필터: open / closed / cancelled")
            @RequestParam(name = "status", required = false) String status,
            @Parameter(description = "장소 키워드 필터")
            @RequestParam(name = "location", required = false) String location,
            @Parameter(description = "모집 인원 필터 (2~4)", example = "4")
            @RequestParam(name = "max_participants", required = false) Integer maxParticipants,
            @Parameter(description = "음식 카테고리 필터 (한식/중식/일식/양식/분식/면류/찌개/경양식)")
            @RequestParam(name = "food_category", required = false) FoodCategory foodCategory
    ) {
        List<PostListResponseDto> posts = postService.getPosts(
                timeSlot,
                status,
                location,
                maxParticipants,
                foodCategory
        );

        return ResponseEntity.ok(posts);
    }

    @Operation(
            summary = "내가 작성한 모집글 조회",
            description = "로그인한 사용자가 호스트로 작성한 모집글 목록을 반환합니다(진행 중인 글이 먼저 정렬)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)")
    })
    @GetMapping("/myposts")
    public ResponseEntity<List<PostListResponseDto>> getMyPosts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();

        List<PostListResponseDto> posts = postService.getMyPosts(email);

        return ResponseEntity.ok(posts);
    }

    @Operation(
            summary = "모집글 상세 조회",
            description = """
                    모집글 단건 상세를 조회합니다.
                    - 참여자 목록, 음식 카테고리, 호스트 정보, 현재 사용자의 참여 상태(join_status)를 포함합니다.
                    - 오픈채팅 링크(kakao_link)는 호스트 또는 참여 중(JOINED)인 사용자에게만 노출됩니다(kakao_link_visible로 표시).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)"),
            @ApiResponse(responseCode = "404", description = "모집글 없음 (POST_404)")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponseDto> getPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "모집글 ID(UUID)") @PathVariable("postId") String postId
    ) {
        String email = userDetails.getUsername();

        PostDetailResponseDto response = postService.getPost(email, postId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "모임 참여",
            description = """
                    해당 모집글에 참여합니다.
                    - 참여 시 현재 인원이 1 증가하며, 참여자에게 오픈채팅 링크가 노출됩니다.
                    - 본인이 만든 모임에는 참여할 수 없고(400), 이미 참여 중이면 400을 반환합니다.
                    - 마감/취소된 모임(409) 또는 인원이 가득 찬 경우(409)에는 참여할 수 없습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "참여 완료", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "본인 모임 참여 불가 / 이미 참여 중 (POST_400_SELF_JOIN, POST_400_ALREADY_JOINED)"),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)"),
            @ApiResponse(responseCode = "404", description = "모집글 없음 (POST_404)"),
            @ApiResponse(responseCode = "409", description = "모집 마감 또는 인원 마감 (POST_409_CLOSED, POST_409_FULL)")
    })
    @PostMapping("/{postId}/join")
    public ResponseEntity<JoinPostResponseDto> joinPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "참여할 모집글 ID(UUID)") @PathVariable("postId") String postId
    ) {
        String email = userDetails.getUsername();

        JoinPostResponseDto response = postService.joinPost(email, postId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "모임 참여 취소",
            description = """
                    참여 중인 모임에서 나갑니다.
                    - 취소 시 현재 인원이 1 감소합니다.
                    - 호스트는 참여 취소가 아니라 모집글 삭제 API를 사용해야 합니다(403).
                    - 식사 시간 30분 전부터는 취소할 수 없습니다(409).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "참여 취소 완료"),
            @ApiResponse(responseCode = "400", description = "참여 중인 모임이 아님 (POST_400_NOT_JOINED)"),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)"),
            @ApiResponse(responseCode = "403", description = "호스트는 취소 불가 (POST_403_FORBIDDEN)"),
            @ApiResponse(responseCode = "404", description = "모집글 없음 (POST_404)"),
            @ApiResponse(responseCode = "409", description = "취소 가능 시간 만료 / 취소된 모임 (POST_409_CANCEL_TIME_EXPIRED)")
    })
    @DeleteMapping("/{postId}/leave")
    public ResponseEntity<Void> cancelJoin(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "참여 취소할 모집글 ID(UUID)") @PathVariable("postId") String postId
    ) {
        String email = userDetails.getUsername();

        postService.cancelJoin(email, postId);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "모집글 수정",
            description = """
                    모집글 내용을 수정합니다. 호스트만 수정할 수 있습니다(403).
                    - 최대 모집 인원은 현재 참여 인원보다 적게 설정할 수 없습니다(400).
                    - 음식 카테고리는 전달한 값으로 전체 교체됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 완료", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "입력값 오류 / 모집 인원 < 현재 인원 (INVALID_REQUEST)"),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)"),
            @ApiResponse(responseCode = "403", description = "호스트가 아님 (POST_403_FORBIDDEN)"),
            @ApiResponse(responseCode = "404", description = "모집글 없음 (POST_404)")
    })
    @PatchMapping("/{postId}")
    public ResponseEntity<PostDetailResponseDto> updatePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "수정할 모집글 ID(UUID)") @PathVariable("postId") String postId,
            @Valid @RequestBody UpdatePostRequestDto requestDto
    ) {
        String email = userDetails.getUsername();

        PostDetailResponseDto response = postService.updatePost(email, postId, requestDto);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "모집글 삭제",
            description = """
                    모집글을 삭제합니다. 호스트만 삭제할 수 있습니다(403).
                    - 삭제 시 참여 중이던 사용자들에게 '모임 취소' 알림이 발송됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 완료"),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)"),
            @ApiResponse(responseCode = "403", description = "호스트가 아님 (POST_403_FORBIDDEN)"),
            @ApiResponse(responseCode = "404", description = "모집글 없음 (POST_404)")
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "삭제할 모집글 ID(UUID)") @PathVariable("postId") String postId
    ) {
        String email = userDetails.getUsername();

        postService.deletePost(email, postId);

        return ResponseEntity.noContent().build();
    }
}
