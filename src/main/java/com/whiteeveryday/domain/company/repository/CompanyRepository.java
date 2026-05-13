package com.whiteeveryday.domain.company.repository;

import com.whiteeveryday.domain.company.entity.Company;
import com.whiteeveryday.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByBusinessNumber(String businessNumber);

    boolean existsByUserId(Long userId);

    Optional<Company> findCompanyByUser(User user);

    Optional<Company> findCompanyByUserId(Long userId);
}
