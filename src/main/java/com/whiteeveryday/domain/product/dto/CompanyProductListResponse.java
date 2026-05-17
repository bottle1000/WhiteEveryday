package com.whiteeveryday.domain.product.dto;

import com.whiteeveryday.domain.product.entity.Product;
import lombok.Getter;

import java.util.List;

@Getter
public class CompanyProductListResponse {

    private List<ProductDetailResponse> products;

    public static CompanyProductListResponse of(List<Product> products) {
        CompanyProductListResponse response = new CompanyProductListResponse();
        response.products = products.stream()
                .map(ProductDetailResponse::from)
                .toList();

        return response;
    }
}
