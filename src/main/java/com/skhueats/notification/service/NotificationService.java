package com.skhueats.notification.service;

import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;
import com.skhueats.global.util.DateTimeUtils;
import com.skhueats.notification.dto.response.NotificationPageResponseDto;
import com.skhueats.notification.dto.response.NotificationResponseDto;
import com.skhueats.notification.entity.Notification;
import com.skhueats.notification.entity.NotificationType;
import com.skhueats.notification.repository.NotificationRepository;
import com.skhueats.user.entity.User;
import com.skhueats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationPageResponseDto getNotifications(String email, int page, int size) {
        User recipient = findUserByEmail(email);
        PageRequest pageRequest = PageRequest.of(normalizePage(page), normalizeSize(size));

        Page<NotificationResponseDto> notifications = notificationRepository
                .findByRecipientOrderByCreatedAtDescIdDesc(recipient, pageRequest)
                .map(NotificationResponseDto::from);

        return NotificationPageResponseDto.from(notifications);
    }

    @Transactional
    public NotificationResponseDto markAsRead(String email, String notificationId) {
        User recipient = findUserByEmail(email);

        Notification notification = notificationRepository.findByIdAndRecipient(notificationId, recipient)
                .orElseThrow(() -> new ApiException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markAsRead();

        return NotificationResponseDto.from(notification);
    }

    @Transactional
    public int markAllAsRead(String email) {
        User recipient = findUserByEmail(email);
        return notificationRepository.markAllAsReadByRecipient(
                recipient,
                DateTimeUtils.nowInKst()
        );
    }

    @Transactional
    public void createNotification(
            User recipient,
            NotificationType type,
            String title,
            String message,
            String targetType,
            String targetId
    ) {
        Notification notification = Notification.of(recipient, type, title, message, targetType, targetId);
        notificationRepository.save(notification);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }

        return Math.min(size, MAX_PAGE_SIZE);
    }
}
