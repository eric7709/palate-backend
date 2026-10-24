package com.app.palate.auth.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.Account;
import com.app.palate.auth.dto.AccountRequestDTO;
import com.app.palate.auth.dto.AuthResponseDTO;
import com.app.palate.auth.utils.AccountMapper;
import com.app.palate.security.JwtUtil;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Creates an account from a signup request and signs the new user in. */
@Service
@RequiredArgsConstructor
public class RegisterAccount {
    private final CreateAccount createAccount;
    private final AccountMapper accountMapper;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponseDTO register(AccountRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getEmail(), "Email");
        ValidationUtils.requireNonBlank(request.getPassword(), "Password");

        Account account = createAccount.create(request);

        return new AuthResponseDTO(
                jwtUtil.generateAccessToken(account),
                jwtUtil.generateRefreshToken(account),
                accountMapper.toMe(account));
    }
}
