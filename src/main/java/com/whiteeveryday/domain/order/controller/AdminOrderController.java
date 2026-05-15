package com.whiteeveryday.domain.order.controller;

import com.whiteeveryday.domain.order.dto.OrderDetailResponse;
import com.whiteeveryday.domain.order.dto.OrderListResponse;
import com.whiteeveryday.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<OrderListResponse> getOrders() {
        OrderListResponse response = orderService.getOrdersForAdmin();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        OrderDetailResponse response = orderService.getOrderDetailForAdmin(orderId);

        return ResponseEntity.ok(response);
    }
}
