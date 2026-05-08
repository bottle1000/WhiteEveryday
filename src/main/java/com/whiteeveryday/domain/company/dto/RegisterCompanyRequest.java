package com.whiteeveryday.domain.company.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RegisterCompanyRequest {

    @NotBlank(message = "기업명 입력은 필수입니다.")
    private String name;

    private String logoUrl;

    @NotBlank(message = "기업 설명은 필수입니다.")
    private String description;

    @NotBlank(message = "사업자 번호 입력은 필수입니다.")
    private String businessNumber;
}
