package com.whiteeveryday.domain.product.entity;

import lombok.Getter;

@Getter
public enum ProductStatus {

    READY, APPROVED, REJECTED, ON_SALE, SOLD_OUT, CLOSED;
}
