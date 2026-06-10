package com.skhueats.notification.service;

import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;
import com.skhueats.notification.dto.response.NotificationResponseDto;
import com.skhueats.notification.entity.Notification;
import com.skhueats.notification.entity.NotificationType;
import com.skhueats.notification.repository.NotificationRepository;
import com.skhueats.user.entity.User;
import com.skhueats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationResponseDto> getNotifications(String email) {
        User recipient = findUserByEmail(email);

        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient).stream()
                .map(NotificationResponseDto::from)
                .toList();
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
}
