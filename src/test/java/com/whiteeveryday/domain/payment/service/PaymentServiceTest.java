package com.whiteeveryday.domain.payment.service;

import com.whiteeveryday.domain.company.entity.Company;
import com.whiteeveryday.domain.order.entity.Order;
import com.whiteeveryday.domain.order.entity.OrderStatus;
import com.whiteeveryday.domain.order.repository.OrderRepository;
import com.whiteeveryday.domain.payment.dto.PaymentConfirmRequest;
import com.whiteeveryday.domain.payment.dto.PaymentConfirmResponse;
import com.whiteeveryday.domain.payment.dto.PaymentFailRequest;
import com.whiteeveryday.domain.payment.dto.PaymentFailResponse;
import com.whiteeveryday.domain.payment.entity.Payment;
import com.whiteeveryday.domain.payment.entity.PaymentStatus;
import com.whiteeveryday.domain.payment.repository.PaymentRepository;
import com.whiteeveryday.domain.payment.toss.TossPaymentClient;
import com.whiteeveryday.domain.payment.toss.TossPaymentConfirmRequest;
import com.whiteeveryday.domain.product.entity.Product;
import com.whiteeveryday.domain.user.entity.Role;
import com.whiteeveryday.domain.user.entity.User;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    TossPaymentClient tossPaymentClient;

    @InjectMocks
    PaymentService paymentService;

    private User user;
    private CustomUserDetails userDetails;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@test.com")
                .encodedPassword("test1234")
                .nickname("tester")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        userDetails = new CustomUserDetails(1L, "test@test.com", "tester", Role.ROLE_USER);

        Company company = Company.builder()
                .name("테스트 기업")
                .description("기업 설명")
                .businessNumber(UUID.randomUUID().toString())
                .user(user)
                .build();
        ReflectionTestUtils.setField(company, "id", 1L);

        product = Product.builder()
                .name("테스트 상품")
                .price(100000)
                .description("상품 설명")
                .stockQuantity(3)
                .saleDate(LocalDate.now())
                .company(company)
                .build();
        ReflectionTestUtils.setField(product, "id", 1L);

        order = Order.builder()
                .user(user)
                .product(product)
                .orderedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);
    }

    @Test
    void 결제_승인_성공() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                order.getId(),
                "payment_key",
                "ORDER_1",
                order.getTotalPrice()
        );

        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderId(order.getId())).willReturn(false);

        // when
        PaymentConfirmResponse result = paymentService.confirm(userDetails, request);

        // then
        assertThat(result.getOrderId()).isEqualTo(order.getId());
        assertThat(result.getPaymentKey()).isEqualTo(request.getPaymentKey());
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(result.getApprovedAt()).isNotNull();
        assertThat(order.getPaidAt()).isNotNull();

        ArgumentCaptor<TossPaymentConfirmRequest> tossCaptor =
                ArgumentCaptor.forClass(TossPaymentConfirmRequest.class);
        verify(tossPaymentClient).confirm(tossCaptor.capture());
        assertThat(tossCaptor.getValue().getPaymentKey()).isEqualTo(request.getPaymentKey());
        assertThat(tossCaptor.getValue().getOrderId()).isEqualTo(request.getOrderIdForPg());
        assertThat(tossCaptor.getValue().getAmount()).isEqualTo(request.getAmount());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(savedPayment.getPaymentKey()).isEqualTo(request.getPaymentKey());
        assertThat(savedPayment.getOrderIdForPg()).isEqualTo(request.getOrderIdForPg());
        assertThat(savedPayment.getAmount()).isEqualTo(request.getAmount());
        assertThat(savedPayment.getOrder()).isEqualTo(order);
        assertThat(savedPayment.getApprovedAt()).isEqualTo(order.getPaidAt());
    }

    @Test
    void 남의_주문_결제_승인_실패() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                order.getId(),
                "payment_key",
                "ORDER_1",
                order.getTotalPrice()
        );
        CustomUserDetails otherUserDetails = new CustomUserDetails(2L, "other@test.com", "other", Role.ROLE_USER);

        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> paymentService.confirm(otherUserDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(tossPaymentClient, never()).confirm(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void PENDING이_아닌_주문_결제_승인_실패() {
        // given
        order.canceled();
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                order.getId(),
                "payment_key",
                "ORDER_1",
                order.getTotalPrice()
        );

        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> paymentService.confirm(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ORDER_STATUS);

        verify(tossPaymentClient, never()).confirm(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void 만료된_주문_결제_승인_실패() {
        // given
        ReflectionTestUtils.setField(order, "expiredAt", LocalDateTime.now().minusSeconds(1));
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                order.getId(),
                "payment_key",
                "ORDER_1",
                order.getTotalPrice()
        );

        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> paymentService.confirm(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_EXPIRED);

        verify(tossPaymentClient, never()).confirm(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void 결제_금액이_다르면_승인_실패() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                order.getId(),
                "payment_key",
                "ORDER_1",
                order.getTotalPrice() - 1000
        );

        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> paymentService.confirm(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_AMOUNT_MISMATCH);

        verify(tossPaymentClient, never()).confirm(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void 이미_결제된_주문이면_승인_실패() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                order.getId(),
                "payment_key",
                "ORDER_1",
                order.getTotalPrice()
        );

        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderId(order.getId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> paymentService.confirm(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verify(tossPaymentClient, never()).confirm(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void Toss_결제_승인_실패시_Payment_저장하지_않음() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                order.getId(),
                "payment_key",
                "ORDER_1",
                order.getTotalPrice()
        );

        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderId(order.getId())).willReturn(false);
        given(tossPaymentClient.confirm(any(TossPaymentConfirmRequest.class)))
                .willThrow(new BusinessException(ErrorCode.PAYMENT_FAILED));

        // when & then
        assertThatThrownBy(() -> paymentService.confirm(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_FAILED);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void 결제_실패_처리_성공() {
        // given
        product.deductStockQuantity();
        PaymentFailRequest request = new PaymentFailRequest(
                order.getId(),
                "payment_key",
                "ORDER_1",
                "카드 승인 실패"
        );

        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderId(order.getId())).willReturn(false);

        // when
        PaymentFailResponse result = paymentService.fail(userDetails, request);

        // then
        assertThat(result.getOrderId()).isEqualTo(order.getId());
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(product.getStockQuantity()).isEqualTo(3);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(savedPayment.getPaymentKey()).isEqualTo(request.getPaymentKey());
        assertThat(savedPayment.getOrderIdForPg()).isEqualTo(request.getOrderIdForPg());
        assertThat(savedPayment.getAmount()).isEqualTo(order.getTotalPrice());
        assertThat(savedPayment.getFailReason()).isEqualTo(request.getReason());
        assertThat(savedPayment.getOrder()).isEqualTo(order);
    }

    @Test
    void 결제_실패_처리시_PG주문번호가_없으면_기본값_사용() {
        // given
        product.deductStockQuantity();
        PaymentFailRequest request = new PaymentFailRequest(
                order.getId(),
                "payment_key",
                null,
                "사용자 결제 이탈"
        );

        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderId(order.getId())).willReturn(false);

        // when
        paymentService.fail(userDetails, request);

        // then
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getOrderIdForPg()).isEqualTo("ORDER_" + order.getId());
    }

    @Test
    void 이미_Payment가_있으면_결제_실패_처리시_중복_저장하지_않음() {
        // given
        product.deductStockQuantity();
        PaymentFailRequest request = new PaymentFailRequest(
                order.getId(),
                "payment_key",
                "ORDER_1",
                "카드 승인 실패"
        );

        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderId(order.getId())).willReturn(true);

        // when
        PaymentFailResponse result = paymentService.fail(userDetails, request);

        // then
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(product.getStockQuantity()).isEqualTo(3);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void PENDING이_아닌_주문_결제_실패_처리_실패() {
        // given
        order.pay(LocalDateTime.now());
        PaymentFailRequest request = new PaymentFailRequest(
                order.getId(),
                "payment_key",
                "ORDER_1",
                "카드 승인 실패"
        );

        given(orderRepository.findOrderByOrderId(order.getId())).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> paymentService.fail(userDetails, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ORDER_STATUS);

        verify(paymentRepository, never()).save(any());
    }
}
