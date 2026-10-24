package com.app.palate.auth.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.auth.dto.AccountRequestDTO;
import com.app.palate.auth.enums.AccountStatus;
import com.app.palate.exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;

/** Saves a new account (new accounts start ACTIVE). Shared by signup and employee creation. */
@Service
@RequiredArgsConstructor
public class CreateAccount {
    private static final String DEFAULT_PASSWORD = "1111111";

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Account create(AccountRequestDTO request) {
        ensureNotTaken(request);

        Account account = new Account();
        account.setEmail(request.getEmail().trim().toLowerCase());
        account.setPassword(passwordEncoder.encode(
                request.getPassword() != null && !request.getPassword().isBlank()
                        ? request.getPassword()
                        : DEFAULT_PASSWORD));
        account.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : null);
        account.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        account.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
        account.setRole(request.getRole());
        account.setStatus(AccountStatus.ACTIVE);
        account.setGender(request.getGender());
        return accountRepository.save(account);
    }

    private void ensureNotTaken(AccountRequestDTO request) {
        if (request.getEmail() != null && accountRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("EMAIL_EXISTS");
        }
        if (request.getPhoneNumber() != null
                && accountRepository.existsByPhoneNumber(request.getPhoneNumber().trim())) {
            throw new BadRequestException("PHONE_EXISTS");
        }
    }
}
