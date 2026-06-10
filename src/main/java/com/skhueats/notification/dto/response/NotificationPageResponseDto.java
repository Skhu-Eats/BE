package com.skhueats.notification.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.springframework.data.domain.Page;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NotificationPageResponseDto(
        List<NotificationResponseDto> notifications,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static NotificationPageResponseDto from(Page<NotificationResponseDto> page) {
        return new NotificationPageResponseDto(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
