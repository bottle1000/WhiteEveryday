package com.whiteeveryday.domain.order.dto;

import com.whiteeveryday.domain.order.entity.Order;
import com.whiteeveryday.domain.order.entity.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderCreateResponse {

    private Long orderId;
    private Long productId;
    private OrderStatus orderStatus;
    private Integer totalPrice;
    private LocalDateTime orderedAt;
    private LocalDateTime expiredAt;

    public static OrderCreateResponse from(Order order) {
        OrderCreateResponse response = new OrderCreateResponse();
        response.orderId = order.getId();
        response.productId = order.getProduct().getId();
        response.orderStatus = order.getOrderStatus();
        response.totalPrice = order.getTotalPrice();
        response.orderedAt = order.getOrderedAt();
        response.expiredAt = order.getExpiredAt();

        return response;
    }
}
