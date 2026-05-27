package com.app.palate.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import com.app.palate.auth.Account;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final String ACCESS_SECRET = "w4QhK7fZ8v+2H1j0p5S6G3bV9nLxTqY4RkF0uJcM1sA=";
    private final String REFRESH_SECRET = "v7NqX1hP9dKzR4tB6sY3F8wL2mJ5cV0nHqT9eR1oGkM=";
    private final long ACCESS_EXPIRATION = 1000 * 60 * 15; // 15 mins
    private final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 days

    // In 0.12.x, use SecretKey interface explicitly instead of generic Key
    private final SecretKey accessTokenSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(ACCESS_SECRET));
    private final SecretKey refreshTokenSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(REFRESH_SECRET));

    // -------------------------------
    // Generate Access Token (SINGLE ROLE)
    // -------------------------------
    public String generateAccessToken(Account account) {
        return Jwts.builder()
                .subject(account.getEmail()) // .setSubject() is now .subject()
                .claim("firstName", account.getFirstName())
                .claim("role", account.getRole().name()) 
                .issuedAt(new Date()) // .setIssuedAt() is now .issuedAt()
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION)) // .setExpiration() is now .expiration()
                .signWith(accessTokenSecret) // standard key signing mechanism
                .compact();
    }

    // -------------------------------
    // Generate Refresh Token
    // -------------------------------
    public String generateRefreshToken(Account account) {
        return Jwts.builder()
                .subject(account.getEmail())
                .claim("id", account.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(refreshTokenSecret)
                .compact();
    }

    // -------------------------------
    // Extract Claims
    // -------------------------------
    private Claims getClaims(String token) {
        // parserBuilder() is gone. Use Jwts.parser().verifyWith(key).build()
        return Jwts.parser()
                .verifyWith(accessTokenSecret)
                .build()
                .parseSignedClaims(token) // parseClaimsJws() is now parseSignedClaims()
                .getPayload(); // getBody() is now getPayload()
    }

    private Claims getRefreshClaims(String token) {
        return Jwts.parser()
                .verifyWith(refreshTokenSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // -------------------------------
    // Extract Email
    // -------------------------------
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String getRefreshEmail(String token) {
        return getRefreshClaims(token).getSubject();
    }

    // -------------------------------
    // Extract User ID
    // -------------------------------
    public Long getUserId(String token) {
        return getClaims(token).get("id", Long.class);
    }

    // -------------------------------
    // Extract Single Role
    // -------------------------------
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // -------------------------------
    // Validate Refresh Token
    // -------------------------------
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

    // -------------------------------
    // Validate Access Token
    // -------------------------------
    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}