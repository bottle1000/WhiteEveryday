package com.whiteeveryday.domain.user.dto;

import lombok.Getter;

@Getter
public class SignUpResponse {
    private Long userId;
    private String email;
    private String nickname;

    public static SignUpResponse of(Long userId, String email, String nickname) {
        SignUpResponse response = new SignUpResponse();
        response.userId = userId;
        response.email = email;
        response.nickname = nickname;

        return response;
    }
}
