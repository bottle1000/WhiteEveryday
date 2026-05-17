package com.whiteeveryday.domain.payment.repository;

import com.whiteeveryday.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByOrderId(Long orderId);

    Optional<Payment> findByOrderId(Long orderId);
}
