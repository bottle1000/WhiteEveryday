package com.whiteeveryday.domain.user.entity;

import com.whiteeveryday.domain.common.BaseEntity;
import com.whiteeveryday.global.exception.BusinessException;
import com.whiteeveryday.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public User (String email, String encodedPassword, String nickname){
        this.email = email;
        this.password = encodedPassword;
        this.nickname = nickname;
        this.role = Role.ROLE_USER;
    }

    public void promoteToCompany() {
        this.role = Role.ROLE_COMPANY;
    }
}
