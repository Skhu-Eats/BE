package com.skhueats.global.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.skhueats.auth.dto.request.RegisterRequestDto;
import com.skhueats.auth.dto.response.LoginResponse;
import com.skhueats.auth.dto.response.RegisterResponseDto;
import com.skhueats.post.dto.request.CreatePostRequestDto;
import com.skhueats.post.dto.request.UpdatePostRequestDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DtoSnakeCaseJsonTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @Test
    void registerRequestDeserializesSnakeCaseFields() throws Exception {
        String json = """
                {
                  "email": "test@skhu.ac.kr",
                  "password": "password123",
                  "nickname": "tester",
                  "department": "Software",
                  "admission_year": 2024,
                  "bio": "hello"
                }
                """;

        RegisterRequestDto request = objectMapper.readValue(json, RegisterRequestDto.class);

        assertThat(request.getAdmissionYear()).isEqualTo(2024);
    }

    @Test
    void registerResponseSerializesSnakeCaseFields() throws Exception {
        RegisterResponseDto response = new RegisterResponseDto(
                "registered",
                "user-1",
                "test@skhu.ac.kr",
                "tester",
                "Software",
                2024,
                "hello",
                true
        );

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"user_id\"");
        assertThat(json).contains("\"admission_year\"");
        assertThat(json).contains("\"email_verified\"");
        assertThat(json).doesNotContain("userId", "admissionYear", "emailVerified");
    }

    @Test
    void loginResponseSerializesSnakeCaseFields() throws Exception {
        LoginResponse response = LoginResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(1800L)
                .nickname("tester")
                .userId("user-1")
                .build();

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"access_token\"");
        assertThat(json).contains("\"refresh_token\"");
        assertThat(json).contains("\"token_type\"");
        assertThat(json).contains("\"expires_in\"");
        assertThat(json).contains("\"user_id\"");
        assertThat(json).doesNotContain("accessToken", "refreshToken", "tokenType", "expiresIn", "userId");
    }

    @Test
    void createPostRequestDeserializesSnakeCaseFields() throws Exception {
        String json = """
                {
                  "title": "Dinner",
                  "food_categories": ["Korean", "Snack"],
                  "location": "School gate",
                  "meeting_time": "2026-06-11T18:00:00",
                  "max_participants": 4,
                  "memo": "Bring appetite",
                  "kakao_link": "https://open.kakao.com/o/example"
                }
                """;

        CreatePostRequestDto request = objectMapper.readValue(json, CreatePostRequestDto.class);

        assertThat(request.getFoodCategories()).containsExactly("Korean", "Snack");
        assertThat(request.getMeetingTime()).isEqualTo(LocalDateTime.of(2026, 6, 11, 18, 0));
        assertThat(request.getMaxParticipants()).isEqualTo(4);
        assertThat(request.getKakaoLink()).isEqualTo("https://open.kakao.com/o/example");
    }

    @Test
    void updatePostRequestDeserializesSnakeCaseFields() throws Exception {
        String json = """
                {
                  "title": "Updated dinner",
                  "food_categories": ["Korean", "Soup"],
                  "location": "Student hall",
                  "meeting_time": "2026-06-12T18:30:00",
                  "max_participants": 3,
                  "memo": "Updated memo",
                  "kakao_link": "https://open.kakao.com/o/updated"
                }
                """;

        UpdatePostRequestDto request = objectMapper.readValue(json, UpdatePostRequestDto.class);

        assertThat(request.getFoodCategories()).containsExactly("Korean", "Soup");
        assertThat(request.getMeetingTime()).isEqualTo(LocalDateTime.of(2026, 6, 12, 18, 30));
        assertThat(request.getMaxParticipants()).isEqualTo(3);
        assertThat(request.getKakaoLink()).isEqualTo("https://open.kakao.com/o/updated");
    }
}
