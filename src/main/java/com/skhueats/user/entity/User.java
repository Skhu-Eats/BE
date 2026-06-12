package com.skhueats.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(nullable = false, length = 50)
    private String department;

    @Column(name = "admission_year", nullable = false, length = 10)
    private Integer admissionYear;

    @Column(length = 255)
    private String bio;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "manner_score", nullable = false)
    private Integer mannerScore = 0;

    @Column(name = "post_count", nullable = false)
    private Integer postCount = 0;

    @Column(name = "join_count", nullable = false)
    private Integer joinCount = 0;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void increasePostCount() {
        if (this.postCount == null) {
            this.postCount = 0;
        }

        this.postCount++;
    }

    public void decreasePostCount() {
        if (this.postCount == null || this.postCount <= 0) {
            this.postCount = 0;
            return;
        }

        this.postCount--;
    }

    public void increaseJoinCount() {
        if (this.joinCount == null) {
            this.joinCount = 0;
        }

        this.joinCount++;
    }

    public void decreaseJoinCount() {
        if (this.joinCount == null || this.joinCount <= 0) {
            this.joinCount = 0;
            return;
        }

        this.joinCount--;
    }
}
