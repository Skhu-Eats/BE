package com.skhueats.user.service;

import com.skhueats.auth.repository.RefreshTokenRepository;
import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;
import com.skhueats.post.entity.Participation;
import com.skhueats.post.entity.ParticipationStatus;
import com.skhueats.post.entity.PostFoodCategory;
import com.skhueats.post.repository.ParticipationRepository;
import com.skhueats.post.repository.PostFoodCategoryRepository;
import com.skhueats.user.dto.request.UpdateMyProfileRequestDto;
import com.skhueats.user.dto.response.MyPageHistoryPreviewResponseDto;
import com.skhueats.user.dto.response.MyPageResponseDto;
import com.skhueats.user.dto.response.MyProfileResponseDto;
import com.skhueats.user.entity.User;
import com.skhueats.user.repository.UserFoodPreferenceRepository;
import com.skhueats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserFoodPreferenceRepository userFoodPreferenceRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ParticipationRepository participationRepository;
    private final PostFoodCategoryRepository postFoodCategoryRepository;

    public MyProfileResponseDto getMyProfile() {
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        return createMyProfileResponse(user);
    }

    public MyPageResponseDto getMyPage(int historyLimit) {
        validateHistoryLimit(historyLimit);

        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        List<String> foodCategories = userFoodPreferenceRepository.findCategoriesByUser(user);
        List<Participation> recentParticipations = participationRepository.findHistoryByUserId(
                user.getId(),
                ParticipationStatus.JOINED,
                PageRequest.of(0, historyLimit)
        ).getContent();

        Map<String, List<String>> categoriesByPostId = findFoodCategoriesByPostId(recentParticipations);
        List<MyPageHistoryPreviewResponseDto> recentHistories = recentParticipations.stream()
                .map(participation -> MyPageHistoryPreviewResponseDto.of(
                        participation,
                        categoriesByPostId.getOrDefault(participation.getPost().getId(), List.of())
                ))
                .toList();

        return MyPageResponseDto.of(user, foodCategories, recentHistories);
    }

    @Transactional
    public MyProfileResponseDto updateMyProfile(UpdateMyProfileRequestDto requestDto) {
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        String newNickname = requestDto.nickname();
        String currentNickname = user.getNickname();

        if (!currentNickname.equals(newNickname)) {
            validateNicknameDuplicate(newNickname);
            user.updateNickname(newNickname);
        }

        return createMyProfileResponse(user);
    }

    @Transactional
    public void deleteAccount() {
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.deleteByEmail(email);
        userFoodPreferenceRepository.deleteAllByUserInBulk(user);
        userRepository.delete(user);
    }

    private MyProfileResponseDto createMyProfileResponse(User user) {
        List<String> foodCategories = userFoodPreferenceRepository.findCategoriesByUser(user);

        return MyProfileResponseDto.from(user, foodCategories);
    }

    private Map<String, List<String>> findFoodCategoriesByPostId(List<Participation> participations) {
        if (participations.isEmpty()) {
            return Map.of();
        }

        List<String> postIds = participations.stream()
                .map(participation -> participation.getPost().getId())
                .toList();

        return postFoodCategoryRepository.findAllByPostIdIn(postIds).stream()
                .collect(Collectors.groupingBy(
                        postFoodCategory -> postFoodCategory.getPost().getId(),
                        Collectors.mapping(PostFoodCategory::getCategory, Collectors.toList())
                ));
    }

    private void validateHistoryLimit(int historyLimit) {
        if (historyLimit < 1 || historyLimit > 20) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "history_limit은 1~20만 허용됩니다.");
        }
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ErrorCode.ACCESS_TOKEN_REQUIRED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        throw new ApiException(ErrorCode.ACCESS_TOKEN_REQUIRED);
    }

    private void validateNicknameDuplicate(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new ApiException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }
}
