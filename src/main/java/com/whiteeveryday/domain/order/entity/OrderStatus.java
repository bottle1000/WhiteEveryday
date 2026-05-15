package com.whiteeveryday.domain.order.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING, PAID, FAILED, EXPIRED, CANCELLED, REFUNDED
}
