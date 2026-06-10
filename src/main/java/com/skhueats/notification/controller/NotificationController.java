package com.skhueats.notification.controller;

import com.skhueats.notification.dto.response.NotificationReadAllResponseDto;
import com.skhueats.notification.dto.response.NotificationResponseDto;
import com.skhueats.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<NotificationResponseDto> response = notificationService.getNotifications(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String notificationId
    ) {
        NotificationResponseDto response = notificationService.markAsRead(
                userDetails.getUsername(),
                notificationId
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<NotificationReadAllResponseDto> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        int updatedCount = notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(NotificationReadAllResponseDto.of(updatedCount));
    }
}
