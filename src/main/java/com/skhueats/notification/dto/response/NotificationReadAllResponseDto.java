package com.skhueats.notification.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NotificationReadAllResponseDto(
        String message,
        int updatedCount
) {

    public static NotificationReadAllResponseDto of(int updatedCount) {
        return new NotificationReadAllResponseDto("알림 전체 읽음 처리 완료", updatedCount);
    }
}
