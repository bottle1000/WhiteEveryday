package com.whiteeveryday.domain.company.service;

import com.whiteeveryday.domain.company.dto.RegisterCompanyRequest;
import com.whiteeveryday.domain.company.dto.RegisterCompanyResponse;
import com.whiteeveryday.domain.company.entity.Company;
import com.whiteeveryday.domain.company.repository.CompanyRepository;
import com.whiteeveryday.domain.user.entity.Role;
import com.whiteeveryday.domain.user.entity.User;
import com.whiteeveryday.domain.user.repository.UserRepository;
import com.whiteeveryday.global.exception.BusinessException;
import com.whiteeveryday.global.exception.ErrorCode;
import com.whiteeveryday.global.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    CompanyRepository companyRepository;

    @InjectMocks
    CompanyService companyService;

    private User user;
    private Company company;
    private CustomUserDetails userDetails;

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

        userDetails = new CustomUserDetails(1L, "test@test.com", "password123", Role.ROLE_USER);
    }

    @Test
    void Company_정상등록() {
        //given
        RegisterCompanyRequest request = new RegisterCompanyRequest("TestCompany", null, "testDescription", "114-55594");
        given(companyRepository.existsByBusinessNumber(request.getBusinessNumber())).willReturn(false);
        given(companyRepository.existsByUserId(userDetails.getId())).willReturn(false);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //when
        RegisterCompanyResponse register = companyService.register(userDetails, request);

        //then
        assertThat(register.getName()).isEqualTo("TestCompany");
        assertThat(user.getRole()).isEqualTo(Role.ROLE_COMPANY);
        verify(companyRepository).save(any(Company.class));
    }
    @Test
    void businessNumber가_이미_있으면_ALREADY_REGISTER_COMPANY() {
        //given
        RegisterCompanyRequest request = new RegisterCompanyRequest("Tester", null, "testDescription", "114-55594");
        given(companyRepository.existsByBusinessNumber("114-55594")).willReturn(true);

        assertThatThrownBy(() -> companyService.register(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_REGISTER_COMPANY);
    }

    @Test
    void user가_이미_company를_가지고_있으면_ALREADY_REGISTER_USER() {
        RegisterCompanyRequest request = new RegisterCompanyRequest("Tester", null, "testDescription", "114-55594");
        given(companyRepository.existsByBusinessNumber(request.getBusinessNumber())).willReturn(false);
        given(companyRepository.existsByUserId(userDetails.getId())).willReturn(true);

        assertThatThrownBy(() -> companyService.register(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_REGISTER_USER);
    }

}