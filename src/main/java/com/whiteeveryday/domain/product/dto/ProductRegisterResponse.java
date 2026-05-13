package com.whiteeveryday.domain.product.dto;

import com.whiteeveryday.domain.product.entity.Product;
import com.whiteeveryday.domain.product.entity.ProductStatus;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ProductRegisterResponse {

    private Long productId;
    private LocalDate saleDate;
    private ProductStatus status;

    public static ProductRegisterResponse from(Product product) {
        ProductRegisterResponse response = new ProductRegisterResponse();
        response.productId = product.getId();
        response.saleDate = product.getSaleDate();
        response.status = product.getProductStatus();
        return response;
    }
}
