package com.app.palate.seed;

import com.app.palate.customer.Customer;
import com.app.palate.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CustomerInitializer {
    private final CustomerRepository customerRepository;

    @Bean
    @Order(2) // Runs after Admin initialization
    CommandLineRunner seedCustomers() {
        return args -> {
            if (customerRepository.count() == 0) {
                Customer c1 = new Customer();
                c1.setName("John Doe");
                c1.setTitle("Mr.");
                c1.setPhoneNumber("+2348012345678");
                c1.setEmail("john.doe@example.com");

                Customer c2 = new Customer();
                c2.setName("Jane Smith");
                c2.setTitle("Ms.");
                c2.setPhoneNumber("+2348098765432");
                c2.setEmail("jane.smith@example.com");

                customerRepository.saveAll(List.of(c1, c2));
                System.out.println("✅ Dummy customers seeded successfully");
            } else {
                System.out.println("ℹ️ Customers already exist, skipping seeding");
            }
        };
    }
}