package com.app.palate.auth.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.Account;
import com.app.palate.auth.dto.AccountRequestDTO;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateEmployee {
    private final CreateAccount createAccount;

    @Transactional
    public Account create(AccountRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getEmail(), "Email");

        return createAccount.create(request);
    }
}
