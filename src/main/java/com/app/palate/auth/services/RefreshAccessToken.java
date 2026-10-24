package com.app.palate.auth.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.auth.dto.AccessTokenResponse;
import com.app.palate.auth.dto.RefreshTokenRequest;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.exceptions.UnauthorizedException;
import com.app.palate.security.JwtUtil;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Issues a new access token for a valid refresh token. */
@Service
@RequiredArgsConstructor
public class RefreshAccessToken {
    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public AccessTokenResponse refresh(RefreshTokenRequest request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.refreshToken(), "Refresh token");

        String refreshToken = request.refreshToken().trim();
        if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = jwtUtil.getRefreshEmail(refreshToken);
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Account not found"));

        return new AccessTokenResponse(jwtUtil.generateAccessToken(account));
    }
}
