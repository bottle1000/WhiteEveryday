package com.whiteeveryday.domain.order.service;

import com.whiteeveryday.domain.company.entity.Company;
import com.whiteeveryday.domain.order.entity.Order;
import com.whiteeveryday.domain.order.entity.OrderStatus;
import com.whiteeveryday.domain.order.repository.OrderRepository;
import com.whiteeveryday.domain.product.entity.Product;
import com.whiteeveryday.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderScheduleServiceTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    OrderScheduleService orderScheduleService;

    private User user;
    private Company company;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@test.com")
                .encodedPassword("test1234")
                .nickname("tester")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        company = Company.builder()
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
    }

    @Test
    void 만료된_PENDING_주문은_EXPIRED_처리하고_재고를_복구한다() {
        // given
        Order order = createOrder(LocalDateTime.now().minusMinutes(11));
        product.deductStockQuantity();
        given(orderRepository.findExpiredPendingOrders(any(LocalDateTime.class))).willReturn(List.of(order));

        // when
        orderScheduleService.expirePendingOrders();

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED);
        assertThat(product.getStockQuantity()).isEqualTo(3);
        verify(orderRepository).findExpiredPendingOrders(any(LocalDateTime.class));
    }

    @Test
    void 만료_대상_주문이_없으면_상태와_재고를_변경하지_않는다() {
        // given
        Order order = createOrder(LocalDateTime.now());
        product.deductStockQuantity();
        given(orderRepository.findExpiredPendingOrders(any(LocalDateTime.class))).willReturn(List.of());

        // when
        orderScheduleService.expirePendingOrders();

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(product.getStockQuantity()).isEqualTo(2);
        verify(orderRepository).findExpiredPendingOrders(any(LocalDateTime.class));
    }

    @Test
    void PENDING이_아닌_주문은_조회되어도_만료_처리하지_않는다() {
        // given
        Order paidOrder = createOrder(LocalDateTime.now().minusMinutes(11));
        product.deductStockQuantity();
        paidOrder.pay(LocalDateTime.now());
        given(orderRepository.findExpiredPendingOrders(any(LocalDateTime.class))).willReturn(List.of(paidOrder));

        // when
        orderScheduleService.expirePendingOrders();

        // then
        assertThat(paidOrder.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(product.getStockQuantity()).isEqualTo(2);
        verify(orderRepository).findExpiredPendingOrders(any(LocalDateTime.class));
    }

    private Order createOrder(LocalDateTime orderedAt) {
        Order order = Order.builder()
                .user(user)
                .product(product)
                .orderedAt(orderedAt)
                .build();
        ReflectionTestUtils.setField(order, "id", 1L);
        return order;
    }
}
