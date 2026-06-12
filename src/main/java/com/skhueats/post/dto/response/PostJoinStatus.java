package com.skhueats.post.dto.response;

public enum PostJoinStatus {
    AVAILABLE("참여하기", true),
    JOINED("이미 참여 중이에요", false),
    FULL("모집이 마감됐어요", false),
    HOST("내가 만든 모임이에요", false);

    private final String buttonLabel;
    private final boolean canJoin;

    PostJoinStatus(String buttonLabel, boolean canJoin) {
        this.buttonLabel = buttonLabel;
        this.canJoin = canJoin;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public boolean canJoin() {
        return canJoin;
    }
}
