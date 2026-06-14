package com.skhueats.post.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.skhueats.global.json.KstLocalDateTimeDeserializer;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreatePostRequestDto {

    @NotBlank(message = "모집글 제목은 필수입니다.")
    @Size(max = 100, message = "모집글 제목은 100자 이하여야 합니다.")
    private String title;

    @NotEmpty(message = "음식 카테고리는 1개 이상 선택해야 합니다.")
    private List<
            @NotBlank(message = "음식 카테고리는 빈 값일 수 없습니다.")
            @Size(max = 30, message = "음식 카테고리는 30자 이하여야 합니다.")
                    String
            > foodCategories;

    @NotBlank(message = "만날 장소는 필수입니다.")
    @Size(max = 100, message = "만날 장소는 100자 이하여야 합니다.")
    private String location;

    @NotNull(message = "모임 시간은 필수입니다.")
    @JsonDeserialize(using = KstLocalDateTimeDeserializer.class)
    private LocalDateTime meetingTime;

    @NotNull(message = "모집 인원은 필수입니다.")
    @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다.")
    @Max(value = 4, message = "모집 인원은 최대 4명까지 가능합니다.")
    private Integer maxParticipants;

    @Size(max = 255, message = "한줄 메모는 255자 이하여야 합니다.")
    private String memo;

    @NotBlank(message = "오픈카톡 링크는 필수입니다.")
    @Size(max = 500, message = "오픈카톡 링크는 500자 이하여야 합니다.")
    private String kakaoLink;
}
