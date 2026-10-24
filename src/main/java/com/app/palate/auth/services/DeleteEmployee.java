package com.app.palate.auth.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.AccountRepository;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteEmployee {
    private final AccountRepository accountRepository;

    @Transactional
    @SuppressWarnings("null")
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Account ID");

        if (!accountRepository.existsById(id)) {
            throw new BadRequestException("Account not found");
        }
        accountRepository.deleteById(id);
    }
}
