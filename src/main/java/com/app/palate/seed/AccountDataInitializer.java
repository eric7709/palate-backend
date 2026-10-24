package com.app.palate.seed;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.auth.enums.AccountStatus;
import com.app.palate.auth.enums.Gender;
import com.app.palate.auth.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AccountDataInitializer {

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    private record AccountSeed(
            String firstName,
            String lastName,
            String email,
            String phone,
            String password,
            Role role,
            Gender gender) {
    }

    private static final List<AccountSeed> ALL_SEEDS = List.of(
            new AccountSeed("Admin", "Palate", "admin@palate.com", "08123221122", "admin123", Role.ROLE_ADMIN,
                    Gender.MALE),
            new AccountSeed("John", "Doe", "john@palate.com", "08011111111", "staff123", Role.ROLE_WAITER, Gender.MALE),
            new AccountSeed("Jane", "Smith", "jane@palate.com", "08022222222", "staff123", Role.ROLE_CASHIER,
                    Gender.FEMALE),
            new AccountSeed("Mike", "Brown", "mike@palate.com", "08033333333", "staff123", Role.ROLE_WAITER,
                    Gender.MALE),
            new AccountSeed("Lucy", "Adams", "lucy@palate.com", "08044444444", "staff123", Role.ROLE_CASHIER,
                    Gender.FEMALE));

    @Bean
    @Order(1)
    CommandLineRunner seedAllAccounts() {
        return args -> {
            for (AccountSeed seed : ALL_SEEDS) {
                try {
                    accountRepository.findByEmail(seed.email().toLowerCase())
                            .or(() -> accountRepository.findByPhoneNumber(seed.phone()))
                            .ifPresentOrElse(
                                    existing -> {
                                        existing.setEmail(seed.email().toLowerCase());
                                        existing.setPassword(passwordEncoder.encode(seed.password()));
                                        existing.setRole(seed.role());
                                        existing.setStatus(AccountStatus.ACTIVE);
                                        accountRepository.save(existing);
                                        log.info("Updated seed account: {}", seed.email());
                                    },
                                    () -> {
                                        Account account = new Account();
                                        account.setFirstName(seed.firstName());
                                        account.setLastName(seed.lastName());
                                        account.setEmail(seed.email().toLowerCase());
                                        account.setPhoneNumber(seed.phone());
                                        account.setPassword(passwordEncoder.encode(seed.password()));
                                        account.setRole(seed.role());
                                        account.setGender(seed.gender());
                                        account.setStatus(AccountStatus.ACTIVE);
                                        accountRepository.save(account);
                                        log.info("Seeded account: {}", seed.email());
                                    });
                } catch (Exception e) {
                    log.warn("⚠️ Failed to seed account {}: {}", seed.email(), e.getMessage());
                }
            }
        };
    }
}