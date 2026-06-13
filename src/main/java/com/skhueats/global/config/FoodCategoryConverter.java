package com.skhueats.global.config;

import com.skhueats.post.entity.FoodCategory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FoodCategoryConverter implements Converter<String, FoodCategory> {

    @Override
    public FoodCategory convert(String source) {
        return FoodCategory.from(source);
    }
}
