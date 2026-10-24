package com.app.palate.auth.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.auth.dto.AccountRequestDTO;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Changes only the fields that are present in the request; anything left out stays as it was. */
@Service
@RequiredArgsConstructor
public class UpdateEmployee {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @SuppressWarnings("null")
    public Account update(Long id, AccountRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Account ID");
        ValidationUtils.requireNonNull(request, "Request body");

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Account not found"));

        if (request.getFirstName() != null) {
            account.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            account.setLastName(request.getLastName().trim());
        }
        if (request.getStatus() != null) {
            account.setStatus(request.getStatus());
        }
        if (request.getGender() != null) {
            account.setGender(request.getGender());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equalsIgnoreCase(account.getEmail())) {
            String sanitizedEmail = request.getEmail().trim().toLowerCase();
            if (accountRepository.existsByEmail(sanitizedEmail)) {
                throw new BadRequestException("EMAIL_EXISTS");
            }
            account.setEmail(sanitizedEmail);
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()
                && !request.getPhoneNumber().equals(account.getPhoneNumber())) {
            String sanitizedPhone = request.getPhoneNumber().trim();
            if (accountRepository.existsByPhoneNumber(sanitizedPhone)) {
                throw new BadRequestException("PHONE_EXISTS");
            }
            account.setPhoneNumber(sanitizedPhone);
        }

        if (request.getRole() != null) {
            account.setRole(request.getRole());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return accountRepository.save(account);
    }
}
