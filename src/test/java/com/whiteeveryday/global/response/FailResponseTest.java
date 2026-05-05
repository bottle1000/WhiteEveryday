package com.whiteeveryday.global.response;

import com.whiteeveryday.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FailResponseTest {

    @Test
    void errorCode로_실패_응답을_생성() {
        FailResponse response = FailResponse.from(ErrorCode.INVALID_REQUEST);

        assertThat(response.getCode()).isEqualTo("INVALID_REQUEST");
        assertThat(response.getMessage()).isEqualTo("잘못된 요청입니다.");
        assertThat(response.getStatus()).isEqualTo(400);
    }
}
