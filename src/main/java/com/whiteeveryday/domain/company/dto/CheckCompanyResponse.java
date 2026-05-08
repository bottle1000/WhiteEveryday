package com.whiteeveryday.domain.company.dto;

import com.whiteeveryday.domain.company.entity.Company;
import lombok.Getter;

@Getter
public class CheckCompanyResponse {

    private Long companyId;
    private String name;
    private String logoUrl;
    private String description;
    private boolean isActive;

    public static CheckCompanyResponse from(Company company) {
        CheckCompanyResponse response = new CheckCompanyResponse();
        response.companyId = company.getId();
        response.name = company.getName();
        response.logoUrl = company.getLogoUrl();
        response.description = company.getDescription();
        response.isActive = company.isActive();

        return response;
    }

}
