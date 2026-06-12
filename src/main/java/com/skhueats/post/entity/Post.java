package com.skhueats.post.entity;

import com.skhueats.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @Column(name = "meeting_time", nullable = false)
    private LocalDateTime meetingTime;

    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants;

    @Column(name = "memo", length = 255)
    private String memo;

    @Column(name = "kakao_link", nullable = false, length = 500)
    private String kakaoLink;

    @Convert(converter = PostStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private PostStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Post(
            User host,
            String title,
            String location,
            LocalDateTime meetingTime,
            Integer maxParticipants,
            String memo,
            String kakaoLink
    ) {
        this.host = host;
        this.title = title;
        this.location = location;
        this.meetingTime = meetingTime;
        this.deadline = meetingTime;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = 1;
        this.memo = memo;
        this.kakaoLink = kakaoLink;
        this.status = PostStatus.OPEN;
    }

    public void update(
            String title,
            String location,
            LocalDateTime meetingTime,
            Integer maxParticipants,
            String memo,
            String kakaoLink
    ) {
        this.title = title;
        this.location = location;
        this.meetingTime = meetingTime;
        this.deadline = meetingTime;
        this.maxParticipants = maxParticipants;
        this.memo = memo;
        this.kakaoLink = kakaoLink;
    }

    public boolean isHostedBy(User user) {
        return this.host.getId().equals(user.getId());
    }

    public boolean isFull() {
        return this.currentParticipants >= this.maxParticipants;
    }

    public boolean isClosed() {
        return this.status != PostStatus.OPEN;
    }

    public void join() {
        this.currentParticipants++;

        if (isFull()) {
            this.status = PostStatus.CLOSED;
        }
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }

        LocalDateTime now = LocalDateTime.now(KST_ZONE);

        if (this.deadline == null) {
            this.deadline = this.meetingTime;
        }

        if (this.currentParticipants == null) {
            this.currentParticipants = 1;
        }

        if (this.status == null) {
            this.status = PostStatus.OPEN;
        }

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now(KST_ZONE);
    }
}
