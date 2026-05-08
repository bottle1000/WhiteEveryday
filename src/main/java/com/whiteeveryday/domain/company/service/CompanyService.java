package com.whiteeveryday.domain.company.service;

import com.whiteeveryday.domain.company.dto.CheckCompanyResponse;
import com.whiteeveryday.domain.company.dto.RegisterCompanyRequest;
import com.whiteeveryday.domain.company.dto.RegisterCompanyResponse;
import com.whiteeveryday.domain.company.entity.Company;
import com.whiteeveryday.domain.company.repository.CompanyRepository;
import com.whiteeveryday.domain.user.entity.User;
import com.whiteeveryday.domain.user.repository.UserRepository;
import com.whiteeveryday.global.exception.BusinessException;
import com.whiteeveryday.global.exception.ErrorCode;
import com.whiteeveryday.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional
    public RegisterCompanyResponse register(CustomUserDetails userDetails, RegisterCompanyRequest request) {

        if (companyRepository.existsByBusinessNumber(request.getBusinessNumber())) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTER_COMPANY);
        }

        if (companyRepository.existsByUserId(userDetails.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTER_USER);
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Company company = Company.builder()
                .name(request.getName())
                .logoUrl(request.getLogoUrl())
                .description(request.getDescription())
                .businessNumber(request.getBusinessNumber())
                .user(user)
                .build();

        user.promoteToCompany();

        try {
            companyRepository.save(company);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTER_COMPANY);
        }

        return RegisterCompanyResponse.from(company);
    }

    @Transactional(readOnly = true)
    public CheckCompanyResponse checkCompany(CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        Company company = companyRepository.findCompanyByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        return CheckCompanyResponse.from(company);
    }
}
