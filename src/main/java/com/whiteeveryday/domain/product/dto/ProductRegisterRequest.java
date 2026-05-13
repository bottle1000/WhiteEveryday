package com.whiteeveryday.domain.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ProductRegisterRequest {

    @NotBlank(message = "이름 입력은 필수입니다.")
    private String name;

    @NotBlank(message = "상품 설명 입력은 필수입니다.")
    private String description;

    @NotNull(message = "가격 입력은 필수입니다.")
    @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "재고 수량 입력은 필수입니다.")
    @Min(value = 1, message = "재고 수량은 최소 1개입니다.")
    @Max(value = 3, message = "재고 수량은 최대 3개입니다.")
    private Integer stockQuantity;

    @NotNull(message = "판매일 입력은 필수입니다.")
    private LocalDate saleDate;
}
