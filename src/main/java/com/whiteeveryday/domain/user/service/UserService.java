package com.whiteeveryday.domain.user.service;

import com.whiteeveryday.domain.user.dto.LoginRequest;
import com.whiteeveryday.domain.user.dto.LoginResponse;
import com.whiteeveryday.domain.user.dto.ReissueTokenRequest;
import com.whiteeveryday.domain.user.dto.SignUpRequest;
import com.whiteeveryday.domain.user.dto.SignUpResponse;
import com.whiteeveryday.domain.user.entity.User;
import com.whiteeveryday.domain.user.repository.UserRepository;
import com.whiteeveryday.global.exception.BusinessException;
import com.whiteeveryday.global.exception.ErrorCode;
import com.whiteeveryday.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.ALREADY_SIGNUP_USER);
        }

        User user = User.builder()
                .email(request.getEmail())
                .encodedPassword(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();

        userRepository.save(user);

        return SignUpResponse.of(user.getId(), user.getEmail(), user.getNickname());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail(), user.getRole());

        return LoginResponse.of(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public LoginResponse reissue(ReissueTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.isValidRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String email = jwtUtil.getEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        String newAccessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String newRefreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail(), user.getRole());

        return LoginResponse.of(newAccessToken, newRefreshToken);
    }
}
