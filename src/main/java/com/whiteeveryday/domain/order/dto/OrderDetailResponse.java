package com.whiteeveryday.domain.order.dto;

import com.whiteeveryday.domain.order.entity.Order;
import com.whiteeveryday.domain.order.entity.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderDetailResponse {
    private Long orderId;
    private Long productId;
    private String productName;
    private OrderStatus orderStatus;
    private Integer totalPrice;
    private LocalDateTime orderedAt;
    private LocalDateTime expiredAt;
    private LocalDateTime paidAt;


    public static OrderDetailResponse from(Order order) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.orderId = order.getId();
        response.productId = order.getProduct().getId();
        response.productName = order.getProduct().getName();
        response.orderStatus = order.getOrderStatus();
        response.totalPrice = order.getTotalPrice();
        response.orderedAt = order.getOrderedAt();
        response.expiredAt = order.getExpiredAt();
        response.paidAt = order.getPaidAt();

        return response;
    }
}
