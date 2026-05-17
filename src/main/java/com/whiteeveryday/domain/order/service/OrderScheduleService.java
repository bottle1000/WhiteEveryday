package com.whiteeveryday.domain.order.service;

import com.whiteeveryday.domain.order.entity.Order;
import com.whiteeveryday.domain.order.entity.OrderStatus;
import com.whiteeveryday.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderScheduleService {

    private final OrderRepository orderRepository;

    @Scheduled(cron = "0 */1 * * * *")
    public void expirePendingOrders() {
        List<Order> expiredOrders =
                orderRepository.findExpiredPendingOrders(LocalDateTime.now());

        for (Order order : expiredOrders) {
            if (order.getOrderStatus() != OrderStatus.PENDING) {
                continue;
            }
            order.expired();
            order.getProduct().addStockQuantity();
        }
    }
}
