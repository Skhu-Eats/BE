package com.skhueats.global.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class KstLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();
        if (hasOffset(trimmed)) {
            return OffsetDateTime.parse(trimmed, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .atZoneSameInstant(KST_ZONE)
                    .toLocalDateTime();
        }

        return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private boolean hasOffset(String value) {
        int timeSeparatorIndex = value.indexOf('T');

        return value.endsWith("Z")
                || value.contains("+")
                || (timeSeparatorIndex != -1 && value.indexOf('-', timeSeparatorIndex) != -1);
    }
}
