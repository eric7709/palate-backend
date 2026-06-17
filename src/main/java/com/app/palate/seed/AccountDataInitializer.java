package com.app.palate.seed;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.auth.AccountStatus;
import com.app.palate.auth.Gender;
import com.app.palate.auth.Role;
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
            Gender gender
    ) {}

    private static final List<AccountSeed> ALL_SEEDS = List.of(
            new AccountSeed("Nathaniel", "Chinonso",  "admin@safronhotel.com", "08123221122",          "admin123",  Role.ROLE_ADMIN,   Gender.MALE),
            new AccountSeed("John",  "Doe",   "john@safronhotel.com",  "08011111111", "staff123",  Role.ROLE_WAITER,  Gender.MALE),
            new AccountSeed("Jane",  "Smith", "jane@safronhotel.com",  "08022222222", "staff123",  Role.ROLE_CASHIER, Gender.FEMALE),
            new AccountSeed("Mike",  "Brown", "mike@safronhotel.com",  "08033333333", "staff123",  Role.ROLE_WAITER,  Gender.MALE),
            new AccountSeed("Lucy",  "Adams", "lucy@safronhotel.com",  "08044444444", "staff123",  Role.ROLE_CASHIER, Gender.FEMALE)
    );

    @Bean
    @Order(1)
    CommandLineRunner seedAllAccounts() {
        return args -> {
            for (AccountSeed seed : ALL_SEEDS) {
                try {
                    accountRepository.findByEmail(seed.email().toLowerCase()).ifPresentOrElse(
                            existing -> log.info("ℹ️ Account already exists, skipping: {}", seed.email()),
                            () -> {
                                Account account = new Account();
                                account.setFirstName(seed.firstName());
                                account.setLastName(seed.lastName());
                                account.setEmail(seed.email().toLowerCase());
                                account.setPassword(passwordEncoder.encode(seed.password()));
                                account.setRole(seed.role());
                                account.setGender(seed.gender());
                                account.setStatus(AccountStatus.ACTIVE);

                                // Only set phone if provided
                                if (seed.phone() != null) {
                                    // Skip if phone already taken
                                    if (accountRepository.existsByPhoneNumber(seed.phone())) {
                                        log.warn("⚠️ Phone {} already exists, saving {} without phone", seed.phone(), seed.email());
                                    } else {
                                        account.setPhoneNumber(seed.phone());
                                    }
                                }

                                accountRepository.save(account);
                                log.info("✅ Seeded account: {} ({})", seed.email(), seed.role());
                            }
                    );
                } catch (Exception e) {
                    log.warn("⚠️ Failed to seed account {}: {}", seed.email(), e.getMessage());
                }
            }
        };
    }
}