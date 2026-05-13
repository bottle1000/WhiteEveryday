package com.whiteeveryday.domain.product.dto;

import com.whiteeveryday.domain.product.entity.Product;
import com.whiteeveryday.domain.product.entity.ProductStatus;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ProductRejectResponse {

    private Long productId;
    private LocalDate saleDate;
    private ProductStatus status;

    public static ProductRejectResponse from(Product product) {
        ProductRejectResponse response = new ProductRejectResponse();
        response.productId = product.getId();
        response.saleDate = product.getSaleDate();
        response.status = product.getProductStatus();

        return response;
    }
}
