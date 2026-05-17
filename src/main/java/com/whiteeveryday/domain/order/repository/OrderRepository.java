package com.whiteeveryday.domain.order.repository;

import com.whiteeveryday.domain.order.entity.Order;
import com.whiteeveryday.domain.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        select count(o) > 0
        from Order o
        where o.user.id = :userId
        and o.saleDate = :saleDate
        and o.orderStatus in :statuses
    """)
    boolean existsByUserIdAndSaleDate(
            @Param("userId") Long userId,
            @Param("saleDate") LocalDate saleDate,
            @Param("statuses") List<OrderStatus> statuses);


    @Query("""
        select o
        from Order o
        join fetch o.product
        where o.user.id = :userId
    """)
    List<Order> findOrdersByUserId(@Param("userId") Long userId);

    @Query("""
        select o
        from Order o
        join fetch o.product
        order by o.orderedAt desc
    """)
    List<Order> findAllOrders();

    @Query("""
        select o
        from Order o
        join fetch o.product
        where o.id = :orderId
    """)
    Optional<Order> findOrderByOrderId(@Param("orderId") Long orderId);

    @Query("""
        select o
        from Order o
        join fetch o.product
        where o.orderStatus = com.whiteeveryday.domain.order.entity.OrderStatus.PENDING
        and o.expiredAt < :now
    """)
    List<Order> findExpiredPendingOrders(@Param("now") LocalDateTime now);
}
