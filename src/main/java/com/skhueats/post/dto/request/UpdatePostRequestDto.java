package com.skhueats.post.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdatePostRequestDto {

    @NotBlank(message = "Post title is required.")
    @Size(max = 100, message = "Post title must be 100 characters or less.")
    private String title;

    @NotEmpty(message = "At least one food category is required.")
    private List<
            @NotBlank(message = "Food category cannot be blank.")
            @Size(max = 30, message = "Food category must be 30 characters or less.")
                    String
            > foodCategories;

    @NotBlank(message = "Meeting location is required.")
    @Size(max = 100, message = "Meeting location must be 100 characters or less.")
    private String location;

    @NotNull(message = "Meeting time is required.")
    @Future(message = "Meeting time must be in the future.")
    private LocalDateTime meetingTime;

    @NotNull(message = "Max participants is required.")
    @Min(value = 2, message = "Max participants must be at least 2.")
    @Max(value = 4, message = "Max participants must be 4 or less.")
    private Integer maxParticipants;

    @Size(max = 255, message = "Memo must be 255 characters or less.")
    private String memo;

    @NotBlank(message = "Kakao link is required.")
    @Size(max = 500, message = "Kakao link must be 500 characters or less.")
    private String kakaoLink;
}
