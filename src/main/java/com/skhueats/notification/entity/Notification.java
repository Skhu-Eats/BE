package com.skhueats.notification.entity;

import com.skhueats.global.util.DateTimeUtils;
import com.skhueats.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "message", nullable = false, length = 255)
    private String message;

    @Column(name = "target_type", length = 30)
    private String targetType;

    @Column(name = "target_id", length = 36)
    private String targetId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Notification(
            User recipient,
            NotificationType type,
            String title,
            String message,
            String targetType,
            String targetId
    ) {
        this.recipient = recipient;
        this.type = type;
        this.title = title;
        this.message = message;
        this.targetType = targetType;
        this.targetId = targetId;
        this.read = false;
    }

    public static Notification of(
            User recipient,
            NotificationType type,
            String title,
            String message,
            String targetType,
            String targetId
    ) {
        return new Notification(recipient, type, title, message, targetType, targetId);
    }

    public void markAsRead() {
        if (this.read) {
            return;
        }

        this.read = true;
        this.readAt = DateTimeUtils.nowInKst();
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }

        if (this.createdAt == null) {
            this.createdAt = DateTimeUtils.nowInKst();
        }
    }
}
