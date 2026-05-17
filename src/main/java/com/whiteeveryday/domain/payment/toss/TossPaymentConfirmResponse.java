package com.whiteeveryday.domain.payment.toss;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossPaymentConfirmResponse {

    private String paymentKey;
    private String orderId;
    private String status;
    private Integer totalAmount;
}
