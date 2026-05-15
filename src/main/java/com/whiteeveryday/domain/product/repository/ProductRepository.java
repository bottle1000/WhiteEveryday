package com.whiteeveryday.domain.product.repository;

import com.whiteeveryday.domain.product.entity.Product;
import com.whiteeveryday.domain.product.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByCompanyIdAndSaleDate(Long companyId, LocalDate saleDate);

    @Query("""
        select p
        from Product p
        where p.id = :productId
        """)
    Optional<Product> findByProductId(@Param("productId") Long productId);

    @Query("""
        select p
        from Product p
        join fetch p.company
        where p.id = :productId
        And p.productStatus in :statuses
        """)
    Optional<Product> findProductByIdAndFilterStatus(Long productId, List<ProductStatus> statuses);

    @Query("""
        select p
        from Product p
        join fetch p.company
        where p.saleDate = :saleDate
        and p.productStatus = :productStatus
        order by p.id asc
        """)
    List<Product> findProductsBySaleDateAndStatus(
            @Param("saleDate")LocalDate saleDate,
            @Param("productStatus") ProductStatus productStatus);

    @Query("""
        select p
        from Product p
        join fetch p.company
        where p.saleDate = :saleDate
        and p.productStatus in :statuses
        order by p.id asc
        """)
    List<Product> findProductsBySaleDateAndFilterStatus(
            @Param("saleDate")LocalDate saleDate,
            @Param("statuses")List<ProductStatus> statuses);
}
