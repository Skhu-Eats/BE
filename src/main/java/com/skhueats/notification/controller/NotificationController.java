package com.skhueats.notification.controller;

import com.skhueats.notification.dto.response.NotificationReadAllResponseDto;
import com.skhueats.notification.dto.response.NotificationPageResponseDto;
import com.skhueats.notification.dto.response.NotificationResponseDto;
import com.skhueats.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "알림 (Notifications)",
        description = "참여/취소/모임 취소 등으로 발생한 알림 조회 및 읽음 처리. "
                + "모든 API는 로그인(Access Token) 후 호출해야 합니다."
)
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "알림 목록 조회 (페이지네이션)",
            description = """
                    로그인한 사용자의 알림을 최신순으로 조회합니다.
                    - page는 0부터 시작합니다(기본 0). size 기본 20, 최대 50.
                    - 각 알림은 읽음 여부(is_read)와 딥링크용 target_type/target_id를 포함합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)"),
            @ApiResponse(responseCode = "404", description = "사용자 없음 (USER_404)")
    })
    @GetMapping
    public ResponseEntity<NotificationPageResponseDto> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "페이지 번호 (0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 50)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        NotificationPageResponseDto response = notificationService.getNotifications(
                userDetails.getUsername(),
                page,
                size
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "알림 단건 읽음 처리",
            description = "지정한 알림을 읽음 상태로 변경합니다. 본인의 알림이 아니거나 존재하지 않으면 404를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "읽음 처리 완료", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)"),
            @ApiResponse(responseCode = "404", description = "알림 없음 (NOTIFICATION_404)")
    })
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "읽음 처리할 알림 ID(UUID)") @PathVariable String notificationId
    ) {
        NotificationResponseDto response = notificationService.markAsRead(
                userDetails.getUsername(),
                notificationId
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "알림 전체 읽음 처리",
            description = "읽지 않은 모든 알림을 한 번에 읽음 처리하고, 처리된 알림 개수(updated_count)를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 읽음 처리 완료 (처리 개수 반환)", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "인증 필요 (AUTH_401_TOKEN_REQUIRED)")
    })
    @PatchMapping("/read-all")
    public ResponseEntity<NotificationReadAllResponseDto> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        int updatedCount = notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(NotificationReadAllResponseDto.of(updatedCount));
    }
}
