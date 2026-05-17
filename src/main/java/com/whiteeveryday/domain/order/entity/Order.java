package com.whiteeveryday.domain.order.entity;

import com.whiteeveryday.domain.common.BaseEntity;
import com.whiteeveryday.domain.product.entity.Product;
import com.whiteeveryday.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "product_id")
    private Product product;

    @Column(updatable = false, name = "ordered_at")
    private LocalDateTime orderedAt;

    @Column(updatable = false, name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Builder
    public Order(User user, Product product, LocalDateTime orderedAt) {
        this.totalPrice = product.getPrice();
        this.saleDate = product.getSaleDate();
        this.orderStatus = OrderStatus.PENDING;
        this.user = user;
        this.product = product;
        this.orderedAt = orderedAt;
        this.expiredAt = orderedAt.plusMinutes(10);
    }

    public void failed() {
        this.orderStatus = OrderStatus.FAILED;
    }

    public void expired() {
        this.orderStatus = OrderStatus.EXPIRED;
    }

    public void canceled() {
        this.orderStatus = OrderStatus.CANCELLED;
    }

    public void refunded() {
        this.orderStatus = OrderStatus.REFUNDED;
    }

    public void pay(LocalDateTime paidAt){
        this.orderStatus = OrderStatus.PAID;
        this.paidAt = paidAt;
    }
}
