package com.whiteeveryday.domain.user.controller;

import com.whiteeveryday.domain.user.dto.LoginRequest;
import com.whiteeveryday.domain.user.dto.LoginResponse;
import com.whiteeveryday.domain.user.dto.ReissueTokenRequest;
import com.whiteeveryday.domain.user.dto.SignUpRequest;
import com.whiteeveryday.domain.user.dto.SignUpResponse;
import com.whiteeveryday.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody @Valid SignUpRequest request) {
        SignUpResponse response = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reissue")
    public ResponseEntity<LoginResponse> reissue(@RequestBody @Valid ReissueTokenRequest request) {
        LoginResponse response = userService.reissue(request);
        return ResponseEntity.ok(response);
    }
}
