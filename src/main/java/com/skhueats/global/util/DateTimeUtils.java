package com.skhueats.global.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class DateTimeUtils {

    public static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    private DateTimeUtils() {
    }

    public static LocalDateTime nowInKst() {
        return LocalDateTime.now(KST_ZONE);
    }
}
