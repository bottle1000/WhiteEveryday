package com.whiteeveryday.domain.payment.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    READY, APPROVED, FAILED, CANCELLED
}
