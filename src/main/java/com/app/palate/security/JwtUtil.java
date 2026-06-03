package com.app.palate.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.app.palate.auth.Account;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.access-secret}")
    private String ACCESS_SECRET;

    @Value("${jwt.refresh-secret}")
    private String REFRESH_SECRET;

    private final long ACCESS_EXPIRATION = 1000 * 60 * 15;
    private final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    private SecretKey accessTokenSecret;
    private SecretKey refreshTokenSecret;

    @PostConstruct
    public void init() {
        accessTokenSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(ACCESS_SECRET));
        refreshTokenSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(REFRESH_SECRET));
    }

    public String generateAccessToken(Account account) {
        return Jwts.builder()
                .subject(account.getEmail())
                .claim("firstName", account.getFirstName())
                .claim("role", account.getRole().name())
                .claim("id", account.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(accessTokenSecret)
                .compact();
    }

    public String generateRefreshToken(Account account) {
        return Jwts.builder()
                .subject(account.getEmail())
                .claim("id", account.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(refreshTokenSecret)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(accessTokenSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Claims getRefreshClaims(String token) {
        return Jwts.parser()
                .verifyWith(refreshTokenSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String getRefreshEmail(String token) {
        return getRefreshClaims(token).getSubject();
    }

    public Long getUserId(String token) {
        return getClaims(token).get("id", Long.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        try {
            Jwts.parser()
                    .verifyWith(refreshTokenSecret)
                    .build()
                    .parseSignedClaims(refreshToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}