package com.app.palate.auth.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.AccountRepository;
import com.app.palate.auth.dto.MeDTO;
import com.app.palate.auth.utils.AccountMapper;
import com.app.palate.auth.utils.AuthHeaderResolver;
import com.app.palate.exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;

/** Returns the account that the access token in the Authorization header belongs to. */
@Service
@RequiredArgsConstructor
public class GetCurrentUser {
    private final AccountRepository accountRepository;
    private final AuthHeaderResolver authHeaderResolver;
    private final AccountMapper accountMapper;

    @Transactional(readOnly = true)
    public MeDTO get(String authHeader) {
        String email = authHeaderResolver.resolveEmail(authHeader);

        return accountRepository.findByEmail(email)
                .map(accountMapper::toMe)
                .orElseThrow(() -> new BadRequestException("Account not found"));
    }
}
