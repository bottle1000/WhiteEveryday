package com.whiteeveryday.global.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    void errorCode를_보관() {
        BusinessException exception = new BusinessException(ErrorCode.INVALID_REQUEST);

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("잘못된 요청입니다.");
    }
}
