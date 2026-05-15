package com.whiteeveryday.domain.order.repository;

import com.whiteeveryday.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        select count(o) > 0
        from Order o
        where o.user.id = :userId
        and o.saleDate = :saleDate
    """)
    boolean existsByUserIdAndSaleDate(
            @Param("userId") Long userId,
            @Param("saleDate") LocalDate saleDate);


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
}
