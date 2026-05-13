package com.whiteeveryday.domain.product.entity;

import com.whiteeveryday.domain.common.BaseEntity;
import com.whiteeveryday.domain.company.entity.Company;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "daily_sale_slots",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_daily_sale_slot_date_number",
                        columnNames = {"sale_date", "slot_number"}
                ),
                @UniqueConstraint(
                        name = "uk_daily_sale_slot_date_company",
                        columnNames = {"sale_date", "company_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DailySaleSlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "sale_date")
    private LocalDate saleDate;

    @Column(nullable = false, name = "slot_number")
    private Integer slotNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DailySaleSlotStatus status;

    @Builder
    public DailySaleSlot(LocalDate saleDate, Integer slotNumber, Company company, Product product) {
        this.saleDate = saleDate;
        this.slotNumber = slotNumber;
        this.company = company;
        this.product = product;
        this.status = DailySaleSlotStatus.RESERVED;
    }

    public void cancel() {
        this.status = DailySaleSlotStatus.CANCELLED;
    }
}
