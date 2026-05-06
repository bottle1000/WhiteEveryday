package com.whiteeveryday.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SignUpRequest {

    @NotBlank(message = "이메일 입력은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호 입력은 필수입니다.")
    private String password;

    @NotBlank(message = "닉네임 입력은 필수입니다.")
    private String nickname;
}
