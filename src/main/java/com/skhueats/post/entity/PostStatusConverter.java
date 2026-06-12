package com.skhueats.post.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PostStatusConverter implements AttributeConverter<PostStatus, String> {

    @Override
    public String convertToDatabaseColumn(PostStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public PostStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            return PostStatus.valueOf(dbData.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("데이터베이스의 모임 상태 값이 올바르지 않습니다: " + dbData, e);
        }
    }
}
