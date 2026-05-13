package com.whiteeveryday.domain.product.controller;

import com.whiteeveryday.domain.product.dto.ProductApproveResponse;
import com.whiteeveryday.domain.product.dto.ProductRegisterResponse;
import com.whiteeveryday.domain.product.dto.ProductRejectResponse;
import com.whiteeveryday.domain.product.entity.Product;
import com.whiteeveryday.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @PatchMapping("/{productId}/approve")
    public ResponseEntity<ProductApproveResponse> approve(@PathVariable Long productId) {
        ProductApproveResponse response = productService.approve(productId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/reject")
    public ResponseEntity<ProductRejectResponse> reject(@PathVariable Long productId) {
        ProductRejectResponse response = productService.reject(productId);

        return ResponseEntity.ok(response);
    }
}
