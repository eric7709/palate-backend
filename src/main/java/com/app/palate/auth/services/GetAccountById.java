package com.app.palate.auth.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAccountById {
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public Account execute(Long id) {
        if(id ==  null){
            throw new BadRequestException("Id is required");
        }
        return accountRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Account not found"));
    }
}
