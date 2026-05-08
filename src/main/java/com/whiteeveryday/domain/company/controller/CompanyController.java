package com.whiteeveryday.domain.company.controller;

import com.whiteeveryday.domain.company.dto.CheckCompanyResponse;
import com.whiteeveryday.domain.company.dto.RegisterCompanyRequest;
import com.whiteeveryday.domain.company.dto.RegisterCompanyResponse;
import com.whiteeveryday.domain.company.service.CompanyService;
import com.whiteeveryday.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<RegisterCompanyResponse> register(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid RegisterCompanyRequest request) {
        RegisterCompanyResponse response = companyService.register(userDetails, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<CheckCompanyResponse> checkCompany(@AuthenticationPrincipal CustomUserDetails userDetails) {
        CheckCompanyResponse response = companyService.checkCompany(userDetails);

        return ResponseEntity.ok(response);
    }
}
