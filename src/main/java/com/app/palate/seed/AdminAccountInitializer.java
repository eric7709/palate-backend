package com.app.palate.seed;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.auth.Gender;
import com.app.palate.auth.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.annotation.Order;

@Configuration
@RequiredArgsConstructor
public class AdminAccountInitializer {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Order(1)
    CommandLineRunner seedAdminAccount() {
        return args -> {
            final String adminEmail = "admin@palate.com";

            accountRepository.findByEmail(adminEmail)
                    .ifPresentOrElse(
                            account -> System.out.println("ℹ️ Admin account already exists"),
                            () -> {
                                Account admin = new Account();
                                admin.setFirstName("Admin");
                                admin.setLastName("User");
                                admin.setEmail(adminEmail); // ← same value
                                admin.setPhoneNumber("+1234567890");
                                admin.setPassword(passwordEncoder.encode("admin123"));
                                admin.setStatus("ACTIVE");
                                admin.setRole(Role.ROLE_ADMIN);
                                admin.setGender(Gender.MALE);
                                accountRepository.save(admin);
                                System.out.println("✅ Admin account created successfully");
                            });
        };
    }
}
