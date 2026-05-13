package com.whiteeveryday.domain.product.dto;

import com.whiteeveryday.domain.product.entity.Product;
import com.whiteeveryday.domain.product.entity.ProductStatus;
import lombok.Getter;

@Getter
public class ProductApproveResponse {

    private Long productId;
    private ProductStatus status;

    public static ProductApproveResponse from(Product product) {
        ProductApproveResponse response = new ProductApproveResponse();
        response.productId = product.getId();
        response.status = product.getProductStatus();

        return response;
    }
}
