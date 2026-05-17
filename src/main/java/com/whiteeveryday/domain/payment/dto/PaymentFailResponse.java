package com.whiteeveryday.domain.payment.dto;

import com.whiteeveryday.domain.order.entity.Order;
import com.whiteeveryday.domain.order.entity.OrderStatus;
import lombok.Getter;

@Getter
public class PaymentFailResponse {

    private Long orderId;
    private OrderStatus orderStatus;

    public static PaymentFailResponse from(Order order) {
        PaymentFailResponse response = new PaymentFailResponse();
        response.orderId = order.getId();
        response.orderStatus = order.getOrderStatus();

        return response;
    }
}
