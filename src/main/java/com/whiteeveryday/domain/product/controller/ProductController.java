package com.whiteeveryday.domain.product.controller;

import com.whiteeveryday.domain.product.dto.CompanyProductListResponse;
import com.whiteeveryday.domain.product.dto.ProductRegisterRequest;
import com.whiteeveryday.domain.product.dto.ProductRegisterResponse;
import com.whiteeveryday.domain.product.service.ProductService;
import com.whiteeveryday.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductRegisterResponse> register(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ProductRegisterRequest request) {

        ProductRegisterResponse register = productService.register(userDetails, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(register);
    }

    @GetMapping
    public ResponseEntity<CompanyProductListResponse> getCompanyProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CompanyProductListResponse response = productService.getCompanyProducts(userDetails);

        return ResponseEntity.ok(response);
    }
}
