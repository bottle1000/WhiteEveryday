package com.whiteeveryday.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReissueTokenRequest {

    @NotBlank(message = "리프레시 토큰 입력은 필수입니다.")
    private String refreshToken;
}
