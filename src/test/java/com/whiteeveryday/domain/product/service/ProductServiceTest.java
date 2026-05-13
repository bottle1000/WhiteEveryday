package com.whiteeveryday.domain.product.service;

import com.whiteeveryday.domain.company.entity.Company;
import com.whiteeveryday.domain.company.repository.CompanyRepository;
import com.whiteeveryday.domain.product.dto.*;
import com.whiteeveryday.domain.product.entity.DailySaleSlot;
import com.whiteeveryday.domain.product.entity.DailySaleSlotStatus;
import com.whiteeveryday.domain.product.entity.Product;
import com.whiteeveryday.domain.product.entity.ProductStatus;
import com.whiteeveryday.domain.product.repository.DailySaleSlotRepository;
import com.whiteeveryday.domain.product.repository.ProductRepository;
import com.whiteeveryday.domain.user.entity.Role;
import com.whiteeveryday.domain.user.entity.User;
import com.whiteeveryday.domain.user.repository.UserRepository;
import com.whiteeveryday.global.exception.BusinessException;
import com.whiteeveryday.global.exception.ErrorCode;
import com.whiteeveryday.global.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    CompanyRepository companyRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    DailySaleSlotRepository dailySaleSlotRepository;

    @InjectMocks
    ProductService productService;

    private User user;
    private Company company;
    private CustomUserDetails userDetails;
    private Product product;


    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@email.com")
                .nickname("tester")
                .encodedPassword("test1234!")
                .build();

        ReflectionTestUtils.setField(user, "id", 1L);

        company = new Company(
                "Tester",
                null,
                "testDescription",
                "114-55594",
                user
        );

        ReflectionTestUtils.setField(company, "id", 1L);

        userDetails = new CustomUserDetails(
                1L,
                "test@test.com",
                "password123",
                Role.ROLE_USER
        );

        product = Product.builder()
                .name("productName")
                .price(100000)
                .stockQuantity(3)
                .description("description")
                .saleDate(LocalDate.now())
                .company(company)
                .build();

        ReflectionTestUtils.setField(product, "id", 1L);
    }


    @Test
    void 상품_등록_성공() {
        //given
        company.activate();

        ProductRegisterRequest request = new ProductRegisterRequest(
                "productName",
                "description",
                100000,
                3,
                LocalDate.now()
        );

        given(userRepository.findById(userDetails.getId())).willReturn(Optional.of(user));

        given(companyRepository.findCompanyByUserId(user.getId())).willReturn(Optional.of(company));

        given(productRepository.existsByCompanyIdAndSaleDate(company.getId(), request.getSaleDate())).willReturn(false);

        //when
        ProductRegisterResponse result = productService.register(userDetails, request);

        //then
        assertThat(result.getStatus()).isEqualTo(ProductStatus.READY);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getName()).isEqualTo("productName");
        assertThat(savedProduct.getDescription()).isEqualTo("description");
        assertThat(savedProduct.getPrice()).isEqualTo(100000);
        assertThat(savedProduct.getStockQuantity()).isEqualTo(3);
        assertThat(savedProduct.getInitialStockQuantity()).isEqualTo(3);
        assertThat(savedProduct.getSaleDate()).isEqualTo(request.getSaleDate());
        assertThat(savedProduct.getProductStatus()).isEqualTo(ProductStatus.READY);
        assertThat(savedProduct.getCompany()).isEqualTo(company);

        verify(dailySaleSlotRepository, never()).save(any());
    }

    @Test
    void 미승인_기업은_상품_등록_실패() {
        //given
        company.deactivate();
        ProductRegisterRequest request = new ProductRegisterRequest(
                "productName",
                "description",
                100000,
                3,
                LocalDate.now()
        );

        given(userRepository.findById(userDetails.getId())).willReturn(Optional.of(user));

        given(companyRepository.findCompanyByUserId(user.getId())).willReturn(Optional.of(company));

        //that
        assertThatThrownBy(() -> productService.register(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMPANY_NOT_ACTIVE);
    }

    @Test
    void 같은_기업은_같은_판매일에_상품_중복_등록_불가(){
        company.activate();
        ProductRegisterRequest request = new ProductRegisterRequest(
                "productName",
                "description",
                100000,
                3,
                LocalDate.now()
        );

        given(userRepository.findById(userDetails.getId())).willReturn(Optional.of(user));

        given(companyRepository.findCompanyByUserId(user.getId())).willReturn(Optional.of(company));

        given(productRepository.existsByCompanyIdAndSaleDate(company.getId(), request.getSaleDate())).willReturn(true);

        //when & then
        assertThatThrownBy(() -> productService.register(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_REGISTERED_PRODUCT);
    }

    @Test
    void 상품_승인_성공() {
        // given
        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));

        given(dailySaleSlotRepository.existsBySaleDateAndCompanyId(product.getSaleDate(), product.getCompany().getId()))
                .willReturn(false);

        given(dailySaleSlotRepository.countBySaleDateAndStatus(product.getSaleDate(), DailySaleSlotStatus.RESERVED))
                .willReturn(0L);

        given(dailySaleSlotRepository.findMaxSlotNumber(product.getSaleDate(), DailySaleSlotStatus.RESERVED))
                .willReturn(Optional.empty());

        //when
        ProductApproveResponse result = productService.approve(product.getId());

        //then
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(ProductStatus.APPROVED);
        assertThat(product.getProductStatus()).isEqualTo(ProductStatus.APPROVED);

        ArgumentCaptor<DailySaleSlot> slotCaptor = ArgumentCaptor.forClass(DailySaleSlot.class);
        verify(dailySaleSlotRepository).save(slotCaptor.capture());

        DailySaleSlot savedSlot = slotCaptor.getValue();

        assertThat(savedSlot.getSaleDate()).isEqualTo(product.getSaleDate());
        assertThat(savedSlot.getSlotNumber()).isEqualTo(1);
        assertThat(savedSlot.getCompany()).isEqualTo(company);
        assertThat(savedSlot.getProduct()).isEqualTo(product);
        assertThat(savedSlot.getStatus()).isEqualTo(DailySaleSlotStatus.RESERVED);
    }

    /**
     * READY가 아닌 상품은 승인 불가
     * - 이미 APPROVED 또는 REJECTED 상태
     * - 승인 시 INVALID_REQUEST
     */

    @Test
    void APPROVED_상품은_승인_불가() {
        //given
        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));

        product.approve();

        // when & then
        assertThatThrownBy(() -> productService.approve(product.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verify(productRepository, never()).save(any());
    }

    @Test
    void REJECTED_상품은_승인_불가() {
        //given
        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));

        product.reject();

        // when & then
        assertThatThrownBy(() -> productService.approve(product.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verify(productRepository, never()).save(any());
    }

    /**
     * 하루 슬롯 10개가 차면 승인 실패
     * - 같은 saleDate에 RESERVED 슬롯 10개 존재
     * - 승인 시 DAILY_SLOT_FULL
     */

    @Test
    void 하루_슬롯_10개가_차면_승인_실패() {
        //given
        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
        given(dailySaleSlotRepository.existsBySaleDateAndCompanyId(product.getSaleDate(), product.getCompany().getId())).willReturn(false);
        given(dailySaleSlotRepository.countBySaleDateAndStatus(product.getSaleDate(),DailySaleSlotStatus.RESERVED)).willReturn(Long.valueOf(10));

        //when & then
        assertThatThrownBy(() -> productService.approve(product.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DAILY_SLOT_FULL);

        verify(dailySaleSlotRepository, never()).save(any());
    }

    /**
     * 상품 반려 성공
     * - READY 상품 반려
     * - 상태가 REJECTED
     */
    @Test
    void 상품_반려_성공() {
        //given
        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));

        //when
        ProductRejectResponse result = productService.reject(product.getId());

        //then
        assertThat(result.getStatus()).isEqualTo(ProductStatus.REJECTED);
    }

    /**
     * READY가 아닌 상품은 반려 불가
     * - APPROVED 상품 반려 시 INVALID_REQUEST
     */

    @Test
    void READY가_아닌_상품은_반려_불가() {
        // given
        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
        product.approve();

        //when & then
        assertThatThrownBy(() -> productService.reject(product.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    /**
     * 오늘의 상품 목록은 ON_SALE만 조회
     * - 같은 날짜에 READY, APPROVED, ON_SALE, SOLD_OUT 섞어두기
     * - /today 서비스 결과에는 ON_SALE만 포함
     */

    @Test
    void 오늘의_상품_목록은_ON_SALE만_조회() {
        //given
        LocalDate today = LocalDate.now();
        product.openSale();
        given(productRepository.findProductsBySaleDateAndStatus(today, ProductStatus.ON_SALE)).willReturn(List.of(product));

        // when
        ProductListResponse result = productService.getTodayProducts();

        // then
        assertThat(result.getSaleDate()).isEqualTo(today);
        assertThat(result.getProducts()).hasSize(1);
        assertThat(result.getProducts().get(0).getStatus()).isEqualTo(ProductStatus.ON_SALE);
        assertThat(result.getProducts().get(0).getName()).isEqualTo("productName");
    }

    /**
     * 특정 날짜 상품 목록은 공개 가능한 상태만 조회
     * - READY, REJECTED는 제외
     * - APPROVED, ON_SALE, SOLD_OUT, CLOSED는 포함
     */
    @Test
    void 특정_날짜_상품_목록은_공개_가능한_상태만_조회() {
        LocalDate saleDate = LocalDate.of(2025, 5, 13);
        product.approve();

        given(productRepository.findProductsBySaleDateAndFilterStatus(saleDate, List.of(ProductStatus.ON_SALE, ProductStatus.APPROVED, ProductStatus.SOLD_OUT, ProductStatus.CLOSED)))
                .willReturn(List.of(product));

        //when
        ProductListResponse result = productService.getSaleDateProducts(saleDate);

        // then
        assertThat(result.getSaleDate()).isEqualTo(saleDate);
        assertThat(result.getProducts()).hasSize(1);
        assertThat(result.getProducts().get(0).getStatus()).isEqualTo(ProductStatus.APPROVED);
        assertThat(result.getProducts().get(0).getName()).isEqualTo("productName");

        verify(productRepository).findProductsBySaleDateAndFilterStatus(saleDate, List.of(ProductStatus.ON_SALE, ProductStatus.APPROVED, ProductStatus.SOLD_OUT, ProductStatus.CLOSED));
    }
}