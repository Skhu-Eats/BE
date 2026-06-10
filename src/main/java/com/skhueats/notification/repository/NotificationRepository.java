package com.skhueats.notification.repository;

import com.skhueats.notification.entity.Notification;
import com.skhueats.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    Page<Notification> findByRecipientOrderByCreatedAtDescIdDesc(User recipient, Pageable pageable);

    Optional<Notification> findByIdAndRecipient(String id, User recipient);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Notification n
            SET n.read = true,
                n.readAt = :readAt
            WHERE n.recipient = :recipient
              AND n.read = false
            """)
    int markAllAsReadByRecipient(
            @Param("recipient") User recipient,
            @Param("readAt") LocalDateTime readAt
    );
}
