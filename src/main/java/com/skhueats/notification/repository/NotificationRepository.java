package com.skhueats.notification.repository;

import com.skhueats.notification.entity.Notification;
import com.skhueats.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    Optional<Notification> findByIdAndRecipient(String id, User recipient);
}
