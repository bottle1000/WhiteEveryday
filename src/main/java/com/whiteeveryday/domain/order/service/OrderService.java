package com.whiteeveryday.domain.order.service;

import com.whiteeveryday.domain.order.dto.*;
import com.whiteeveryday.domain.order.entity.Order;
import com.whiteeveryday.domain.order.entity.OrderStatus;
import com.whiteeveryday.domain.order.repository.OrderRepository;
import com.whiteeveryday.domain.product.entity.Product;
import com.whiteeveryday.domain.product.entity.ProductStatus;
import com.whiteeveryday.domain.product.repository.ProductRepository;
import com.whiteeveryday.domain.user.entity.User;
import com.whiteeveryday.domain.user.repository.UserRepository;
import com.whiteeveryday.global.exception.BusinessException;
import com.whiteeveryday.global.exception.ErrorCode;
import com.whiteeveryday.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderCreateResponse orderCreate(CustomUserDetails userDetails, OrderCreateRequest request) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getProductStatus() != ProductStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.SALE_NOT_OPEN);
        }

        if (orderRepository.existsByUserIdAndSaleDate(
                user.getId(),
                product.getSaleDate(),
                List.of(OrderStatus.PENDING, OrderStatus.PAID))){
            throw new BusinessException(ErrorCode.ALREADY_ORDERED_TODAY);
        }

        product.deductStockQuantity();

        Order order = Order.builder()
                .user(user)
                .product(product)
                .orderedAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);

        return OrderCreateResponse.from(order);
    }


    // 내 주문 목록 조회
    @Transactional(readOnly = true)
    public OrderListResponse getMyOrders(CustomUserDetails userDetails) {
        if (!userRepository.existsById(userDetails.getId())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        List<Order> orders = orderRepository.findOrdersByUserId(userDetails.getId());

        return OrderListResponse.of(orders);
    }

    // 주문 상세 조회
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(CustomUserDetails userDetails, Long orderId) {
        if (!userRepository.existsById(userDetails.getId())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Order order = orderRepository.findOrderByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userDetails.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return OrderDetailResponse.from(order);
    }

    public OrderCancelResponse orderCancel(CustomUserDetails userDetails, Long orderId) {
        if (!userRepository.existsById(userDetails.getId())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Order order = orderRepository.findOrderByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userDetails.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }
        Product product = order.getProduct();

        order.canceled();
        product.addStockQuantity();

        return OrderCancelResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderListResponse getOrdersForAdmin() {
        List<Order> orders = orderRepository.findAllOrders();

        return OrderListResponse.of(orders);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetailForAdmin(Long orderId) {
        Order order = orderRepository.findOrderByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        return OrderDetailResponse.from(order);
    }
}
