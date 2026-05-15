package com.whiteeveryday.domain.order.controller;

import com.whiteeveryday.domain.order.dto.*;
import com.whiteeveryday.domain.order.service.OrderService;
import com.whiteeveryday.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> orderCreate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid OrderCreateRequest request){

        OrderCreateResponse response = orderService.orderCreate(userDetails, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<OrderListResponse> getMyOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderListResponse orders = orderService.getMyOrders(userDetails);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        OrderDetailResponse response = orderService.getOrderDetail(userDetails, orderId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderCancelResponse> orderCancel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        OrderCancelResponse response = orderService.orderCancel(userDetails, orderId);

        return ResponseEntity.ok(response);
    }
}
