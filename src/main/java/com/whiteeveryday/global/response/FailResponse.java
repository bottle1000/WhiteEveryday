package com.whiteeveryday.global.response;


import com.whiteeveryday.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class FailResponse {

    private final String code;
    private final String message;
    private final int status;

    public FailResponse(ErrorCode errorCode) {
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
        this.status = errorCode.getStatus();
    }

    public static FailResponse from(ErrorCode errorCode) {
        return new FailResponse(errorCode);
    }
}
