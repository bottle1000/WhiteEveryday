package com.whiteeveryday.domain.order.dto;

import com.whiteeveryday.domain.order.entity.Order;
import com.whiteeveryday.domain.order.entity.OrderStatus;
import lombok.Getter;

@Getter
public class OrderCancelResponse {

    private Long orderId;
    private OrderStatus orderStatus;

    public static OrderCancelResponse from(Order order) {
        OrderCancelResponse response = new OrderCancelResponse();
        response.orderId = order.getId();
        response.orderStatus = order.getOrderStatus();

        return response;
    }
}
