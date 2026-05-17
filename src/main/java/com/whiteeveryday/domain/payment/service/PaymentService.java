package com.whiteeveryday.domain.payment.service;

import com.whiteeveryday.domain.order.entity.Order;
import com.whiteeveryday.domain.order.entity.OrderStatus;
import com.whiteeveryday.domain.order.repository.OrderRepository;
import com.whiteeveryday.domain.payment.dto.PaymentConfirmRequest;
import com.whiteeveryday.domain.payment.dto.PaymentConfirmResponse;
import com.whiteeveryday.domain.payment.dto.PaymentFailRequest;
import com.whiteeveryday.domain.payment.dto.PaymentFailResponse;
import com.whiteeveryday.domain.payment.entity.Payment;
import com.whiteeveryday.domain.payment.repository.PaymentRepository;
import com.whiteeveryday.domain.payment.toss.TossPaymentClient;
import com.whiteeveryday.domain.payment.toss.TossPaymentConfirmRequest;
import com.whiteeveryday.global.exception.BusinessException;
import com.whiteeveryday.global.exception.ErrorCode;
import com.whiteeveryday.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TossPaymentClient tossPaymentClient;

    public PaymentConfirmResponse confirm(CustomUserDetails userDetails, PaymentConfirmRequest request) {
        Order order = getOrder(request.getOrderId());

        validateOrderOwner(userDetails, order);
        validatePendingOrder(order);
        validateNotExpired(order);
        validateAmount(order, request.getAmount());
        validateNotPaid(order);

        tossPaymentClient.confirm(new TossPaymentConfirmRequest(
                request.getPaymentKey(),
                request.getOrderIdForPg(),
                request.getAmount()
        ));

        Payment payment = Payment.builder()
                .paymentKey(request.getPaymentKey())
                .orderIdForPg(request.getOrderIdForPg())
                .amount(request.getAmount())
                .order(order)
                .build();

        LocalDateTime approvedAt = LocalDateTime.now();
        payment.approve(approvedAt);
        order.pay(approvedAt);

        paymentRepository.save(payment);

        return PaymentConfirmResponse.from(payment);
    }

    public PaymentFailResponse fail(CustomUserDetails userDetails, PaymentFailRequest request) {
        Order order = getOrder(request.getOrderId());

        validateOrderOwner(userDetails, order);
        validatePendingOrder(order);

        order.failed();
        order.getProduct().addStockQuantity();

        if (!paymentRepository.existsByOrderId(order.getId())) {
            Payment payment = Payment.builder()
                    .paymentKey(request.getPaymentKey())
                    .orderIdForPg(resolveOrderIdForPg(request.getOrderIdForPg(), order))
                    .amount(order.getTotalPrice())
                    .order(order)
                    .build();
            payment.fail(request.getReason());
            paymentRepository.save(payment);
        }

        return PaymentFailResponse.from(order);
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findOrderByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void validateOrderOwner(CustomUserDetails userDetails, Order order) {
        if (!order.getUser().getId().equals(userDetails.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void validatePendingOrder(Order order) {
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }

    private void validateNotExpired(Order order) {
        if (order.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ORDER_EXPIRED);
        }
    }

    private void validateAmount(Order order, Integer amount) {
        if (!order.getTotalPrice().equals(amount)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private void validateNotPaid(Order order) {
        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private String resolveOrderIdForPg(String orderIdForPg, Order order) {
        if (orderIdForPg != null && !orderIdForPg.isBlank()) {
            return orderIdForPg;
        }

        return "ORDER_" + order.getId();
    }
}
