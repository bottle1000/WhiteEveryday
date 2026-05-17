package com.whiteeveryday.domain.payment.entity;

import com.whiteeveryday.domain.common.BaseEntity;
import com.whiteeveryday.domain.order.entity.Order;
import com.whiteeveryday.global.exception.BusinessException;
import com.whiteeveryday.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_key", unique = true)
    private String paymentKey;

    @Column(name = "order_id_for_pg", nullable = false)
    private String orderIdForPg;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "fail_reason")
    private String failReason;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;


    @Builder
    public Payment(String paymentKey, String orderIdForPg, Integer amount, Order order) {
        this.paymentKey = paymentKey;
        this.orderIdForPg = orderIdForPg;
        this.amount = amount;
        this.paymentStatus = PaymentStatus.READY;
        this.order = order;
    }

    //결제 승인 엔티티 메서드
    public void approve(LocalDateTime approvedAt) {
        if (this.paymentStatus != PaymentStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        this.paymentStatus = PaymentStatus.APPROVED;
        this.approvedAt = approvedAt;
    }

    // 결제 실패 엔티티 메서드
    public void fail(String failReason) {
        if (this.paymentStatus != PaymentStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        this.paymentStatus = PaymentStatus.FAILED;
        this.failReason = failReason;
    }

    // 결제 취소 엔티티 메서드
    public void cancel(String failReason) {
        if (this.paymentStatus != PaymentStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        this.paymentStatus = PaymentStatus.CANCELLED;
        this.failReason = failReason;
    }
}
