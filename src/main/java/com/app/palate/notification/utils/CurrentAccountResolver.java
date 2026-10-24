package com.app.palate.notification.utils;

import org.springframework.stereotype.Component;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.security.JwtUtil;

import lombok.RequiredArgsConstructor;

/** Finds the account that owns the "Bearer" token in an Authorization header. */
@Component
@RequiredArgsConstructor
public class CurrentAccountResolver {
    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;

    public Account resolve(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Missing or invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String email = jwtUtil.getEmail(token);
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Account not found"));
    }
}
