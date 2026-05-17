package com.whiteeveryday.domain.payment.controller;

import com.whiteeveryday.domain.payment.dto.PaymentConfirmRequest;
import com.whiteeveryday.domain.payment.dto.PaymentConfirmResponse;
import com.whiteeveryday.domain.payment.dto.PaymentFailRequest;
import com.whiteeveryday.domain.payment.dto.PaymentFailResponse;
import com.whiteeveryday.domain.payment.service.PaymentService;
import com.whiteeveryday.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PaymentConfirmRequest request) {
        PaymentConfirmResponse response = paymentService.confirm(userDetails, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/fail")
    public ResponseEntity<PaymentFailResponse> fail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PaymentFailRequest request) {
        PaymentFailResponse response = paymentService.fail(userDetails, request);

        return ResponseEntity.ok(response);
    }
}
