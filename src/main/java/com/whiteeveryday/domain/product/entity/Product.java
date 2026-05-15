package com.whiteeveryday.domain.product.entity;

import com.whiteeveryday.domain.common.BaseEntity;
import com.whiteeveryday.domain.company.entity.Company;
import com.whiteeveryday.global.exception.BusinessException;
import com.whiteeveryday.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_company_sale_date",
                        columnNames = {"company_id", "sale_date"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false, name = "stock_quantity")
    private Integer stockQuantity;

    @Column(nullable = false, name = "initial_stock_quantity")
    private Integer initialStockQuantity;

    @Column(nullable = false, name = "sale_date")
    private LocalDate saleDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus productStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Builder
    Product(String name, String description, Integer price, Integer stockQuantity, LocalDate saleDate, Company company) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.initialStockQuantity = stockQuantity;
        this.saleDate = saleDate;
        this.productStatus = ProductStatus.READY;
        this.company = company;
    }

    public void approve(){
        this.productStatus = ProductStatus.APPROVED;
    }
    public void reject() {
        this.productStatus = ProductStatus.REJECTED;
    }
    public void openSale() {
        this.productStatus = ProductStatus.ON_SALE;
    }
    public void closeSale() {
        this.productStatus = ProductStatus.CLOSED;
    }

    public void deductStockQuantity() {
        if (this.stockQuantity <= 0) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }

        this.stockQuantity -= 1;

        if (this.stockQuantity == 0) {
            this.productStatus = ProductStatus.SOLD_OUT;
        }
    }

    public void addStockQuantity() {
        this.stockQuantity += 1;

        if (this.initialStockQuantity < this.stockQuantity) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (this.stockQuantity > 0 && this.productStatus == ProductStatus.SOLD_OUT) {
            this.productStatus = ProductStatus.ON_SALE;
        }
    }
}
