package com.whiteeveryday.global.jwt;

import com.whiteeveryday.domain.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final SecretKey secretKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-ms}") long accessTokenValidityMs,
            @Value("${jwt.refresh-token-validity-ms}") long refreshTokenValidityMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    public String createAccessToken(Long userId, String email, Role role) {
        return createToken(userId, email, role, ACCESS_TOKEN_TYPE, accessTokenValidityMs);
    }

    public String createRefreshToken(Long userId, String email, Role role) {
        return createToken(userId, email, role, REFRESH_TOKEN_TYPE, refreshTokenValidityMs);
    }

    public boolean isValidAccessToken(String token) {
        return isValidToken(token, ACCESS_TOKEN_TYPE);
    }

    public boolean isValidRefreshToken(String token) {
        return isValidToken(token, REFRESH_TOKEN_TYPE);
    }

    private boolean isValidToken(String token, String tokenType) {
        try {
            Claims claims = parseClaims(token);
            return tokenType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    private String createToken(Long userId, String email, Role role, String tokenType, long validityMs) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validityMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role.name())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
