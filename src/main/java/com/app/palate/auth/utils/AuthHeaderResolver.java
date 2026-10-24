package com.app.palate.auth.utils;

import org.springframework.stereotype.Component;

import com.app.palate.exceptions.UnauthorizedException;
import com.app.palate.security.JwtUtil;

import lombok.RequiredArgsConstructor;

/** Reads the "Bearer ..." Authorization header, checks the access token, and returns who it belongs to. */
@Component
@RequiredArgsConstructor
public class AuthHeaderResolver {
    private final JwtUtil jwtUtil;

    public String resolveEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        if (!jwtUtil.isValid(token)) {
            throw new UnauthorizedException("Access token expired or invalid");
        }
        return jwtUtil.getEmail(token);
    }
}
