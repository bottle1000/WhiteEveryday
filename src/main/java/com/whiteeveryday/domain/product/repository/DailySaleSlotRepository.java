package com.whiteeveryday.domain.product.repository;


import com.whiteeveryday.domain.product.entity.DailySaleSlot;
import com.whiteeveryday.domain.product.entity.DailySaleSlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailySaleSlotRepository extends JpaRepository<DailySaleSlot, Long> {

    boolean existsBySaleDateAndCompanyId(LocalDate saleDate, Long companyId);

    @Query(
            """
            select count(s.id)
            from DailySaleSlot s
            where s.saleDate = :saleDate
            and s.status = :status
            """
    )
    long countBySaleDateAndStatus(
            @Param("saleDate") LocalDate saleDate,
            @Param("status") DailySaleSlotStatus status
    );

    @Query("""
        select max(s.slotNumber)
        from DailySaleSlot s
        where s.saleDate = :saleDate
        and s.status = :status
    """)
    Optional<Integer> findMaxSlotNumber(
            @Param("saleDate")LocalDate saleDate,
            @Param("status")DailySaleSlotStatus status
    );
}
