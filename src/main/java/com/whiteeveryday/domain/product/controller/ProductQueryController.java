package com.whiteeveryday.domain.product.controller;

import com.whiteeveryday.domain.product.dto.ProductDetailResponse;
import com.whiteeveryday.domain.product.dto.ProductListResponse;
import com.whiteeveryday.domain.product.service.ProductService;
import com.whiteeveryday.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductQueryController {

    private final ProductService productService;

    @GetMapping("/today")
    public ResponseEntity<ProductListResponse> getTodayProducts() {
        ProductListResponse response = productService.getTodayProducts();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ProductListResponse> getSaleDateProducts(
            @RequestParam("saleDate") LocalDate saleDate) {
        ProductListResponse response = productService.getSaleDateProducts(saleDate);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getDetailProduct(@PathVariable Long productId) {
        ProductDetailResponse response = productService.getDetailProduct(productId);

        return ResponseEntity.ok(response);
    }
}
