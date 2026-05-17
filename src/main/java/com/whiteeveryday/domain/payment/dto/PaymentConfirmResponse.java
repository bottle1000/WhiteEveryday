package com.whiteeveryday.domain.payment.dto;

import com.whiteeveryday.domain.order.entity.OrderStatus;
import com.whiteeveryday.domain.payment.entity.Payment;
import com.whiteeveryday.domain.payment.entity.PaymentStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentConfirmResponse {

    private Long orderId;
    private String paymentKey;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;
    private LocalDateTime approvedAt;

    public static PaymentConfirmResponse from(Payment payment) {
        PaymentConfirmResponse response = new PaymentConfirmResponse();
        response.orderId = payment.getOrder().getId();
        response.paymentKey = payment.getPaymentKey();
        response.paymentStatus = payment.getPaymentStatus();
        response.orderStatus = payment.getOrder().getOrderStatus();
        response.approvedAt = payment.getApprovedAt();

        return response;
    }
}
