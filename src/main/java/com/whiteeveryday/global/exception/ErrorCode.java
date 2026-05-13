package com.whiteeveryday.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    INVALID_REQUEST("잘못된 요청입니다.", 400),
    UNAUTHORIZED("인증되지 않은 사용자입니다.", 401),
    FORBIDDEN("접근 권한이 없습니다.", 403),
    ALREADY_SIGNUP_USER("이미 가입된 유저입니다.", 409),
    ALREADY_REGISTER_COMPANY("이미 등록된 기업입니다.", 409),
    ALREADY_REGISTER_USER("이미 기업을 가지고 있습니다.", 409),
    COMPANY_NOT_ACTIVE("승인되지 않은 기업입니다.", 403),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", 404),
    COMPANY_NOT_FOUND("기업(회사)를 찾을 수 없습니다.", 404),
    PRODUCT_NOT_FOUND("상품을 찾을 수 없습니다.", 404),
    ORDER_NOT_FOUND("주문을 찾을 수 없습니다.", 404),
    OUT_OF_STOCK("상품 재고가 없습니다.", 409),
    ALREADY_ORDERED_TODAY("오늘 이미 주문한 상품입니다.", 409),
    SALE_NOT_OPEN("판매 전입니다.", 409),
    SALE_CLOSED("판매가 완료되었습니다.", 409),
    DAILY_SLOT_FULL("슬롯이 가득찼습니다.", 409),
    ALREADY_REGISTERED_PRODUCT("이미 등록된 상품입니다.", 409),
    PAYMENT_AMOUNT_MISMATCH("결제 금액이 일치하지 않습니다.", 409),
    PAYMENT_FAILED("결제를 실패했습니다.", 409),
    ORDER_EXPIRED("주문이 유효하지 않습니다.", 409),
    INVALID_ORDER_STATUS("유효하지 않은 주문 상태입니다.", 409),
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다.", 500);

    private final String message;
    private final int status;

    ErrorCode(String message, int status) {
        this.message = message;
        this.status = status;
    }
}
