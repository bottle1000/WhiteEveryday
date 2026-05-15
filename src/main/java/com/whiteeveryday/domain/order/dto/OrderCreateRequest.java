package com.whiteeveryday.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;
}
