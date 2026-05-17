package com.whiteeveryday.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentFailRequest {

    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;

    private String paymentKey;

    private String orderIdForPg;

    @NotBlank(message = "실패 사유는 필수입니다.")
    private String reason;
}
