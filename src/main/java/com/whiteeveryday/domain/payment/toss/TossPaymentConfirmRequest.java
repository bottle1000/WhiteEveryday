package com.whiteeveryday.domain.payment.toss;

import lombok.Getter;

@Getter
public class TossPaymentConfirmRequest {

    private final String paymentKey;
    private final String orderId;
    private final Integer amount;

    public TossPaymentConfirmRequest(String paymentKey, String orderId, Integer amount) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
    }
}
