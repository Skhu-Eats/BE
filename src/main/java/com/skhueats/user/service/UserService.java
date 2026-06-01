package com.skhueats.user.service;

import com.skhueats.global.exception.ApiException;
import com.skhueats.global.exception.ErrorCode;
import com.skhueats.user.dto.request.UpdateMyProfileRequestDto;
import com.skhueats.user.dto.response.MyProfileResponseDto;
import com.skhueats.user.entity.User;
import com.skhueats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public MyProfileResponseDto getMyProfile() {
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        return MyProfileResponseDto.from(user);
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

        return MyProfileResponseDto.from(user);
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
