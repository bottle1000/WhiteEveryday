package com.whiteeveryday.domain.order.service;

import com.whiteeveryday.domain.company.entity.Company;
import com.whiteeveryday.domain.order.dto.*;
import com.whiteeveryday.domain.order.entity.Order;
import com.whiteeveryday.domain.order.entity.OrderStatus;
import com.whiteeveryday.domain.order.repository.OrderRepository;
import com.whiteeveryday.domain.product.entity.Product;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    OrderService orderService;

    private User user;
    private CustomUserDetails userDetails;
    private Company company;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        // 유저 관련
        user = User.builder()
                .email("test@test.com")
                .encodedPassword("test1234")
                .nickname("tester")
                .build();

        ReflectionTestUtils.setField(user, "id", 1L);

        userDetails = new CustomUserDetails(1L, "test@test.com", "tester", Role.ROLE_USER);

        // 기업 관련
        company = Company.builder()
                .name("테스트 기업")
                .description("기업 설명")
                .businessNumber(UUID.randomUUID().toString())
                .user(user)
                .build();

        ReflectionTestUtils.setField(company, "id", 1L);

        // 상품 관련
        product = Product.builder()
                .name("테스트 상품")
                .price(100000)
                .description("상품 설명")
                .stockQuantity(3)
                .saleDate(LocalDate.now())
                .company(company)
                .build();

        ReflectionTestUtils.setField(product, "id", 1L);

        // 주문 관련
        order = Order.builder()
                .user(user)
                .product(product)
                .orderedAt(LocalDateTime.now())
                .build();

        ReflectionTestUtils.setField(order, "id", 1L);
    }

    /**
     * 1. 주문 생성 성공
     * ON_SALE 상품
     * 기존 주문 없음
     * 주문 상태 PENDING
     * totalPrice는 상품 가격
     * saleDate는 상품 판매일
     * 재고 1 감소
     * expiredAt = orderedAt + 10분
     */
    @Test
    void 주문_생성_성공() {
        // given
        product.openSale();
        OrderCreateRequest request = new OrderCreateRequest(product.getId());
        given(userRepository.findById(userDetails.getId())).willReturn(Optional.of(user));
        given(productRepository.findById(request.getProductId())).willReturn(Optional.of(product));
        given(orderRepository.existsByUserIdAndSaleDate(
                user.getId(),
                product.getSaleDate(),
                List.of(OrderStatus.PENDING, OrderStatus.PAID)
        )).willReturn(false);

        // when
        OrderCreateResponse result = orderService.orderCreate(userDetails, request);

        //then
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getProductId()).isEqualTo(product.getId());
        assertThat(result.getTotalPrice()).isEqualTo(product.getPrice());
        assertThat(product.getStockQuantity()).isEqualTo(2);


        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getUser()).isEqualTo(user);
        assertThat(savedOrder.getProduct()).isEqualTo(product);
        assertThat(savedOrder.getSaleDate()).isEqualTo(product.getSaleDate());
        assertThat(savedOrder.getTotalPrice()).isEqualTo(product.getPrice());
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(savedOrder.getOrderedAt()).isNotNull();
        assertThat(savedOrder.getExpiredAt()).isEqualTo(savedOrder.getOrderedAt().plusMinutes(10));
    }

    /**
     * 2. 판매 중이 아닌 상품 주문 실패
     * ◦ READY, APPROVED, CLOSED 등
     * ◦ SALE_NOT_OPEN
     */
    @Test
    void 판매_중이_아닌_상품_주문_실패() {
        //given
        OrderCreateRequest request = new OrderCreateRequest(product.getId());
        given(userRepository.findById(userDetails.getId())).willReturn(Optional.of(user));
        given(productRepository.findById(request.getProductId())).willReturn(Optional.of(product));

        //when & then
        assertThatThrownBy(() -> orderService.orderCreate(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SALE_NOT_OPEN);
    }

    /**
     * 3. 이미 같은 판매일 주문이 있으면 실패
     * ◦ PENDING 또는 PAID 주문이 있으면 existsByUserIdAndSaleDate()가 true
     * ◦ ALREADY_ORDERED_TODAY
     */
    @Test
    void 이미_같은_판매일_주문이_있으면_실패() {
        //given
        product.openSale();
        OrderCreateRequest request = new OrderCreateRequest(product.getId());
        given(userRepository.findById(userDetails.getId())).willReturn(Optional.of(user));
        given(productRepository.findById(request.getProductId())).willReturn(Optional.of(product));
        given(orderRepository.existsByUserIdAndSaleDate(
                user.getId(),
                product.getSaleDate(),
                List.of(OrderStatus.PENDING, OrderStatus.PAID)
        )).willReturn(true);

        // when & then
        assertThatThrownBy(() -> orderService.orderCreate(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_ORDERED_TODAY);
    }

    /** 4.재고가 없으면 주문 실패
     * ◦ stockQuantity 0
     * ◦OUT_OF_STOCK
     * ◦ 주문 저장 안 됨
     */
    @Test
    void 재고가_없으면_주문_실패() {
        // given
        product.openSale();
        ReflectionTestUtils.setField(product, "stockQuantity", 0);

        OrderCreateRequest request = new OrderCreateRequest(product.getId());

        given(userRepository.findById(userDetails.getId())).willReturn(Optional.of(user));
        given(productRepository.findById(request.getProductId())).willReturn(Optional.of(product));
        given(orderRepository.existsByUserIdAndSaleDate(
                user.getId(),
                product.getSaleDate(),
                List.of(OrderStatus.PENDING, OrderStatus.PAID)
        )).willReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.orderCreate(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OUT_OF_STOCK);

        verify(orderRepository, never()).save(any());
    }

    /** 5. 내 주문 목록 조회 성공
     * ◦ 로그인 유저 ID로 repository 조회
     * ◦ 응답에 주문 목록 변환 확인
     */
    @Test
    void 내_주문_목록_조회_성공() {
        // given
        given(userRepository.existsById(userDetails.getId())).willReturn(true);
        given(orderRepository.findOrdersByUserId(userDetails.getId())).willReturn(List.of(order));

        // when
        OrderListResponse result = orderService.getMyOrders(userDetails);

        // then
        assertThat(result.getOrders()).hasSize(1);
        assertThat(result.getOrders().get(0).getOrderId()).isEqualTo(order.getId());
        assertThat(result.getOrders().get(0).getProductName()).isEqualTo(product.getName());
        assertThat(result.getOrders().get(0).getTotalPrice()).isEqualTo(product.getPrice());
        assertThat(result.getOrders().get(0).getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getOrders().get(0).getOrderedAt()).isEqualTo(order.getOrderedAt());

        verify(orderRepository).findOrdersByUserId(userDetails.getId());
    }

    /** 6. 주문 상세 조회 성공
     * ◦ 본인 주문이면 상세 응답 반환
     */
    @Test
    void 주문_상세_조회_성공() {
        // given
        given(userRepository.existsById(userDetails.getId())).willReturn(true);
        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));

        // when
        OrderDetailResponse result = orderService.getOrderDetail(userDetails, order.getId());

        // then
        assertThat(result.getOrderId()).isEqualTo(order.getId());
        assertThat(result.getProductId()).isEqualTo(product.getId());
        assertThat(result.getProductName()).isEqualTo(product.getName());
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getTotalPrice()).isEqualTo(product.getPrice());
        assertThat(result.getOrderedAt()).isEqualTo(order.getOrderedAt());
        assertThat(result.getExpiredAt()).isEqualTo(order.getExpiredAt());
        assertThat(result.getPaidAt()).isNull();
    }

    /** 7. 남의 주문 상세 조회 실패
     * ◦ 주문의 userId와 로그인 userId 다름
     * ◦ FORBIDDEN
     */
    @Test
    void 남의_주문_상세_조회_실패() {
        // given
        User otherUser = User.builder()
                .email("other@test.com")
                .encodedPassword("test1234")
                .nickname("other")
                .build();
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        Order otherOrder = Order.builder()
                .user(otherUser)
                .product(product)
                .orderedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(otherOrder, "id", 2L);

        given(userRepository.existsById(userDetails.getId())).willReturn(true);
        given(orderRepository.findOrderByOrderId(otherOrder.getId())).willReturn(Optional.of(otherOrder));

        // when & then
        assertThatThrownBy(() -> orderService.getOrderDetail(userDetails, otherOrder.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void 주문_취소_성공() {
        //given
        given(userRepository.existsById(userDetails.getId())).willReturn(true);
        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));
        ReflectionTestUtils.setField(product, "stockQuantity",2);

        // when
        OrderCancelResponse result = orderService.orderCancel(userDetails, order.getId());

        //then
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void PENDING이_아닌_주문_취소_실패() {
        //given
        given(userRepository.existsById(userDetails.getId())).willReturn(true);
        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));
        order.expired();

        //when & then
        assertThatThrownBy(() -> orderService.orderCancel(userDetails, order.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ORDER_STATUS);
    }

    @Test
    void 관리자_주문_목록_조회_성공() {
        // given
        given(orderRepository.findAllOrders()).willReturn(List.of(order));

        // when
        OrderListResponse result = orderService.getOrdersForAdmin();

        // then
        assertThat(result.getOrders()).hasSize(1);
        assertThat(result.getOrders().get(0).getOrderId()).isEqualTo(order.getId());
        assertThat(result.getOrders().get(0).getProductName()).isEqualTo(product.getName());
        assertThat(result.getOrders().get(0).getTotalPrice()).isEqualTo(product.getPrice());
        assertThat(result.getOrders().get(0).getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getOrders().get(0).getOrderedAt()).isEqualTo(order.getOrderedAt());

        verify(orderRepository).findAllOrders();
    }

    @Test
    void 관리자_주문_상세_조회_성공() {
        // given
        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));

        // when
        OrderDetailResponse result = orderService.getOrderDetailForAdmin(order.getId());

        // then
        assertThat(result.getOrderId()).isEqualTo(order.getId());
        assertThat(result.getProductId()).isEqualTo(product.getId());
        assertThat(result.getProductName()).isEqualTo(product.getName());
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getTotalPrice()).isEqualTo(product.getPrice());
        assertThat(result.getOrderedAt()).isEqualTo(order.getOrderedAt());
        assertThat(result.getExpiredAt()).isEqualTo(order.getExpiredAt());
        assertThat(result.getPaidAt()).isNull();

        verify(orderRepository).findOrderByOrderId(order.getId());
    }
}
