package com.app.palate.auth.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.auth.dto.ChangePasswordRequestDTO;
import com.app.palate.auth.dto.MessageResponse;
import com.app.palate.auth.utils.AuthHeaderResolver;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Changes the signed-in user's password after checking the current one. */
@Service
@RequiredArgsConstructor
public class ChangePassword {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthHeaderResolver authHeaderResolver;

    @Transactional
    public MessageResponse change(String authHeader, ChangePasswordRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.currentPassword(), "Current password");
        ValidationUtils.requireNonBlank(request.newPassword(), "New password");

        String email = authHeaderResolver.resolveEmail(authHeader);

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Account not found"));

        if (!passwordEncoder.matches(request.currentPassword(), account.getPassword())) {
            throw new BadRequestException("INVALID_CURRENT_PASSWORD");
        }
        if (passwordEncoder.matches(request.newPassword(), account.getPassword())) {
            throw new BadRequestException("NEW_PASSWORD_SAME_AS_OLD");
        }

        account.setPassword(passwordEncoder.encode(request.newPassword()));
        accountRepository.save(account);
        return new MessageResponse("Password changed successfully");
    }
}
