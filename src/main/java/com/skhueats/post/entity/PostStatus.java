package com.skhueats.post.entity;

public enum PostStatus {
    OPEN("모집 중"),
    CLOSED("완료"),
    CANCELLED("취소됨");

    private final String description;

    PostStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
