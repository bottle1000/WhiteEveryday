package com.whiteeveryday.domain.user.entity;

import lombok.Getter;

@Getter
public enum Role {

    ROLE_USER("일반 유저"),
    ROLE_ADMIN("관리자"),
    ROLE_COMPANY("기업");

    private final String name;

    Role(String name) {
        this.name = name;
    }
}
