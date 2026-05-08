package com.whiteeveryday.domain.company.dto;

import com.whiteeveryday.domain.company.entity.Company;
import lombok.Getter;

@Getter
public class RegisterCompanyResponse {

    private Long companyId;
    private String name;
    private boolean isActive;

    public static RegisterCompanyResponse from(Company company) {
        RegisterCompanyResponse response = new RegisterCompanyResponse();
        response.companyId = company.getId();
        response.name = company.getName();
        response.isActive = company.isActive();

        return response;
    }
}
