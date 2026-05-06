package com.whiteeveryday.domain.user.dto;

import lombok.Getter;

@Getter
public class LoginResponse {

    private String accessToken;
    private String refreshToken;

    public static LoginResponse of(String accessToken, String refreshToken) {
        LoginResponse response = new LoginResponse();
        response.accessToken = accessToken;
        response.refreshToken = refreshToken;

        return response;
    }
}
