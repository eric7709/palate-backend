package com.app.palate.seed;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.auth.AccountStatus;
import com.app.palate.auth.Gender;
import com.app.palate.auth.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;@Configuration
@RequiredArgsConstructor
public class AccountDataInitializer {
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    private static final List<AccountSeed> ACCOUNT_SEEDS = List.of(
            new AccountSeed("John", "Doe", "john.waiter@palate.com", "08011111111", Role.ROLE_WAITER, Gender.MALE),
            new AccountSeed("Jane", "Smith", "jane.cashier@palate.com", "08022222222", Role.ROLE_CASHIER, Gender.FEMALE),
            new AccountSeed("Mike", "Brown", "mike.waiter@palate.com", "08033333333", Role.ROLE_WAITER, Gender.MALE),
            new AccountSeed("Lucy", "Adams", "lucy.cashier@palate.com", "08044444444", Role.ROLE_CASHIER, Gender.FEMALE)
    );

    @Bean
    @Order(5)
    CommandLineRunner seedAccounts() {
        return args -> {
            List<String> emails = ACCOUNT_SEEDS.stream().map(a -> a.email().toLowerCase()).toList();
            List<String> existingEmails = accountRepository.findExistingEmails(emails);

            List<Account> toSave = ACCOUNT_SEEDS.stream()
                    .filter(a -> !existingEmails.contains(a.email().toLowerCase()))
                    .map(seed -> {
                        Account account = new Account();
                        account.setFirstName(seed.firstName());
                        account.setLastName(seed.lastName());
                        account.setEmail(seed.email());
                        account.setPhoneNumber(seed.phone());
                        account.setRole(seed.role());
                        account.setGender(seed.gender()); // Essential: set gender
                        account.setStatus(AccountStatus.ACTIVE); // Essential: set status
                        account.setPassword(passwordEncoder.encode("staff123"));
                        return account;
                    })
                    .toList();
            if (!toSave.isEmpty()) {
                accountRepository.saveAll(toSave);
                System.out.println("✅ Seeded " + toSave.size() + " accounts");
            }
        };
    }

    private record AccountSeed(
            String firstName, String lastName, String email, String phone, Role role, Gender gender
    ) {}
}