package com.whiteeveryday.domain.company.entity;

import com.whiteeveryday.domain.common.BaseEntity;
import com.whiteeveryday.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "companies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, name = "business_number", unique = true)
    private String businessNumber;

    @Column(nullable = false, name = "is_active")
    private boolean isActive;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder
    public Company(String name, String logoUrl, String description, String businessNumber, User user){
        this.name = name;
        this.logoUrl = logoUrl;
        this.description = description;
        this.businessNumber = businessNumber;
        this.isActive = false;
        this.user = user;
    }
}
