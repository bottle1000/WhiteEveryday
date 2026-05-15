package com.whiteeveryday.domain.order.dto;

import com.whiteeveryday.domain.order.entity.Order;
import com.whiteeveryday.domain.order.entity.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderListResponse {

    private List<OrderSummaryResponse> orders;

    public static OrderListResponse of(List<Order> orders) {
        OrderListResponse response = new OrderListResponse();

        response.orders = orders.stream()
                .map(OrderSummaryResponse::from)
                .toList();

        return response;
    }



    @Getter
    public static class OrderSummaryResponse {
        private Long orderId;
        private String productName;
        private Integer totalPrice;
        private OrderStatus orderStatus;
        private LocalDateTime orderedAt;

        public static OrderSummaryResponse from (Order order) {
            OrderSummaryResponse response = new OrderSummaryResponse();
            response.orderId = order.getId();
            response.productName = order.getProduct().getName();
            response.totalPrice = order.getTotalPrice();
            response.orderStatus = order.getOrderStatus();
            response.orderedAt = order.getOrderedAt();

            return response;
        }
    }
}
