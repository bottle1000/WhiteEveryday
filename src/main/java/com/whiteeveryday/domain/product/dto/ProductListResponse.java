package com.whiteeveryday.domain.product.dto;

import com.whiteeveryday.domain.product.entity.Product;
import com.whiteeveryday.domain.product.entity.ProductStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class ProductListResponse {

    private LocalDate saleDate;
    private List<ProductSummaryResponse> products;

    public static ProductListResponse of(LocalDate saleDate, List<Product> products) {
        ProductListResponse response = new ProductListResponse();
        response.saleDate = saleDate;
        response.products = products.stream()
                .map(ProductSummaryResponse::from)
                .toList();

        return response;
    }

    @Getter
    public static class ProductSummaryResponse {

        private Long productId;
        private String companyName;
        private String name;
        private String imageUrl;
        private Integer price;
        private Integer stockQuantity;
        private ProductStatus status;

        private static ProductSummaryResponse from(Product product) {
            ProductSummaryResponse response = new ProductSummaryResponse();
            response.productId = product.getId();
            response.companyName = product.getCompany().getName();
            response.name = product.getName();
            response.imageUrl = product.getImageUrl();
            response.price = product.getPrice();
            response.stockQuantity = product.getStockQuantity();
            response.status = product.getProductStatus();

            return response;
        }
    }

}
