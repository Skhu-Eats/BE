package com.skhueats.post.dto.response;

import com.skhueats.post.entity.Post;
import com.skhueats.post.entity.PostStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;

public enum PostResponseStatus {
    OPEN("모집 중"),
    CLOSING_SOON("마감 임박"),
    CLOSED("완료"),
    CANCELLED("취소됨");

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
    private static final long CLOSING_SOON_MINUTES = 30;

    private final String description;

    PostResponseStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static PostResponseStatus from(Post post) {
        return from(post, LocalDateTime.now(KST_ZONE));
    }

    public static PostResponseStatus from(Post post, LocalDateTime now) {
        if (post.getStatus() == PostStatus.CANCELLED) {
            return CANCELLED;
        }

        if (post.getStatus() == PostStatus.CLOSED || post.isFull() || !post.getDeadline().isAfter(now)) {
            return CLOSED;
        }

        if (!post.getDeadline().isAfter(now.plusMinutes(CLOSING_SOON_MINUTES))) {
            return CLOSING_SOON;
        }

        return OPEN;
    }
}
