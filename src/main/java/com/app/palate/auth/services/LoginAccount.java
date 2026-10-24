package com.app.palate.auth.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.auth.dto.AuthResponseDTO;
import com.app.palate.auth.dto.LoginRequestDTO;
import com.app.palate.auth.utils.AccountMapper;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.security.JwtUtil;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginAccount {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.email(), "Email");
        ValidationUtils.requireNonBlank(request.password(), "Password");

        Account account = accountRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), account.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        return new AuthResponseDTO(
                jwtUtil.generateAccessToken(account),
                jwtUtil.generateRefreshToken(account),
                accountMapper.toMe(account));
    }
}
