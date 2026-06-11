package com.skhueats.post.entity;

import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;

public enum TimeSlot {

    LUNCH(11, 14),
    DINNER(17, 20);

    private final int startHour;
    private final int endHour;

    TimeSlot(int startHour, int endHour) {
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getEndHour() {
        return endHour;
    }

    /**
     * time_slot 쿼리 파라미터를 변환한다.
     * null / 빈 값 / "all" 은 시간대 필터 없음을 의미하므로 null을 반환한다.
     * lunch / dinner 는 해당 enum을 반환하고, 그 외 값은 예외를 던진다.
     */
    public static TimeSlot from(String value) {
        if (value == null || value.isBlank() || value.trim().equalsIgnoreCase("all")) {
            return null;
        }
        try {
            return TimeSlot.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "time_slot은 lunch/dinner/all만 허용됩니다.");
        }
    }
}
