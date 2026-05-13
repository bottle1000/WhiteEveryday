package com.whiteeveryday.domain.product.dto;

import com.whiteeveryday.domain.product.entity.Product;
import com.whiteeveryday.domain.product.entity.ProductStatus;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ProductDetailResponse {

    private Long productId;
    private Long companyId;
    private String companyName;
    private String name;
    private String description;
    private Integer price;
    private Integer stockQuantity;
    private LocalDate saleDate;
    private ProductStatus status;

    public static ProductDetailResponse from(Product product) {
        ProductDetailResponse response = new ProductDetailResponse();
        response.productId = product.getId();
        response.companyId = product.getCompany().getId();
        response.companyName = product.getCompany().getName();
        response.name = product.getName();
        response.description = product.getDescription();
        response.price = product.getPrice();
        response.stockQuantity = product.getStockQuantity();
        response.saleDate = product.getSaleDate();
        response.status = product.getProductStatus();

        return response;
    }
}
