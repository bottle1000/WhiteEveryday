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
import com.whiteeveryday.domain.user.entity.User;
import com.whiteeveryday.domain.user.repository.UserRepository;
import com.whiteeveryday.global.exception.BusinessException;
import com.whiteeveryday.global.exception.ErrorCode;
import com.whiteeveryday.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final DailySaleSlotRepository dailySaleSlotRepository;

    @Transactional
    public ProductRegisterResponse register(CustomUserDetails userDetails, ProductRegisterRequest request) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Company company = companyRepository.findCompanyByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        if (!company.isActive()) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_ACTIVE);
        }

        if (productRepository.existsByCompanyIdAndSaleDate(company.getId(), request.getSaleDate())) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_PRODUCT);
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .saleDate(request.getSaleDate())
                .company(company)
                .build();

        try {
            productRepository.save(product);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_PRODUCT);
        }

        return ProductRegisterResponse.from(product);
    }

    public CompanyProductListResponse getCompanyProducts(CustomUserDetails userDetails) {
        Company company = companyRepository.findCompanyByUserId(userDetails.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        List<Product> products = productRepository.findProductsByCompanyId(company.getId());

        return CompanyProductListResponse.of(products);
    }

    // 상품 상세 조회
    public ProductDetailResponse getDetailProduct(Long productId) {
        Product product = productRepository.findProductByIdAndFilterStatus(
                        productId,
                        List.of(ProductStatus.ON_SALE,
                                ProductStatus.APPROVED,
                                ProductStatus.SOLD_OUT,
                                ProductStatus.CLOSED))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        return ProductDetailResponse.from(product);
    }

    public ProductListResponse getTodayProducts() {
        LocalDate today = LocalDate.now();
        List<Product> products = productRepository.findProductsBySaleDateAndStatus(
                today,
                ProductStatus.ON_SALE
        );

        return ProductListResponse.of(today, products);
    }

    public ProductListResponse getSaleDateProducts(LocalDate saleDate) {
        List<Product> products = productRepository.findProductsBySaleDateAndFilterStatus(
                saleDate,
                List.of(ProductStatus.ON_SALE,
                        ProductStatus.APPROVED,
                        ProductStatus.SOLD_OUT,
                        ProductStatus.CLOSED));

        return ProductListResponse.of(saleDate, products);
    }

    /**
     * 관리자용
     * @param productId
     * @return
     */
    @Transactional
    public ProductApproveResponse approve(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getProductStatus() != ProductStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (dailySaleSlotRepository.existsBySaleDateAndCompanyId(product.getSaleDate(), product.getCompany().getId())) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_PRODUCT);
        }

        long slotCount = dailySaleSlotRepository.countBySaleDateAndStatus(
                product.getSaleDate(),
                DailySaleSlotStatus.RESERVED
        );

        if (slotCount >= 10) {
            throw new BusinessException(ErrorCode.DAILY_SLOT_FULL);
        }

        Integer nextSlotNumber = dailySaleSlotRepository
                .findMaxSlotNumber(product.getSaleDate(), DailySaleSlotStatus.RESERVED)
                .map(maxSlotNumber -> maxSlotNumber + 1)
                .orElse(1);

        product.approve();

        DailySaleSlot slot = DailySaleSlot.builder()
                .saleDate(product.getSaleDate())
                .slotNumber(nextSlotNumber)
                .company(product.getCompany())
                .product(product)
                .build();

        try {
            dailySaleSlotRepository.save(slot);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DAILY_SLOT_FULL);
        }

        return ProductApproveResponse.from(product);
    }

    @Transactional
    public ProductRejectResponse reject(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getProductStatus() != ProductStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        product.reject();

        return ProductRejectResponse.from(product);
    }


}
