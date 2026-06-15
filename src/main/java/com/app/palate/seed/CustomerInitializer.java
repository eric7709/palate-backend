package com.app.palate.seed;

import com.app.palate.customer.Customer;
import com.app.palate.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;


@Configuration
@RequiredArgsConstructor
@Slf4j 
public class CustomerInitializer {
    private final CustomerRepository customerRepository;

    @Bean
    @Order(2) // Runs after Admin initialization
    CommandLineRunner seedCustomers() {
        return args -> {
            seedIfAbsent("+2348012345678", c -> {
                c.setName("John Doe");
                c.setTitle("Mr.");
                c.setEmail("john.doe@example.com");
            });

            seedIfAbsent("+2348098765432", c -> {
                c.setName("Jane Smith");
                c.setTitle("Ms.");
                c.setEmail("jane.smith@example.com");
            });
        };
    }

    private void seedIfAbsent(String phone, java.util.function.Consumer<Customer> configure) {
        customerRepository.findByPhoneNumber(phone).ifPresentOrElse(
                existing -> log.info("ℹ️ Customer already exists, skipping: {}", phone),
                () -> {
                    Customer c = new Customer();
                    c.setPhoneNumber(phone);
                    configure.accept(c);
                    customerRepository.save(c);
                    log.info("✅ Seeded customer: {}", phone);
                });
    }
}