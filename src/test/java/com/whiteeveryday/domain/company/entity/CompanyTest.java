package com.whiteeveryday.domain.company.entity;

import com.whiteeveryday.domain.company.dto.CheckCompanyResponse;
import com.whiteeveryday.domain.company.dto.RegisterCompanyResponse;
import com.whiteeveryday.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;


class CompanyTest {

    private User user;
    private Company company;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@test.com")
                .encodedPassword("password123")
                .nickname("tester")
                .build();

        ReflectionTestUtils.setField(user, "id", 1L);

        company = new Company("Tester", null, "testDescription", "114-55594", user);

        ReflectionTestUtils.setField(company, "id", 1L);
    }

    @Test
    void 기업_생성시_isActive는_false이고_logoUrl은_null일_수_있다() {
        assertThat(company.isActive()).isEqualTo(false);
        assertThat(company.getLogoUrl()).isNull();
    }

    @Test
    void RegisterCompanyResponse() {
        RegisterCompanyResponse response = RegisterCompanyResponse.from(company);

        assertThat(response.getCompanyId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Tester");
        assertThat(response.isActive()).isEqualTo(false);
    }

    @Test
    void 내_기업_조회_응답은_logoUrl이_null일_수_있다() {
        CheckCompanyResponse response = CheckCompanyResponse.from(company);

        assertThat(response.getLogoUrl()).isEqualTo(null);
    }
}