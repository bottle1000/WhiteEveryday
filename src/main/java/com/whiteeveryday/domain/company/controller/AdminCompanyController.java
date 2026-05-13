package com.whiteeveryday.domain.company.controller;

import com.whiteeveryday.domain.company.dto.RegisterCompanyResponse;
import com.whiteeveryday.domain.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
public class AdminCompanyController {

    private final CompanyService companyService;

    @PatchMapping("/{companyId}/approve")
    public ResponseEntity<RegisterCompanyResponse> approve(@PathVariable Long companyId) {
        RegisterCompanyResponse response = companyService.approve(companyId);

        return ResponseEntity.ok(response);
    }
}
