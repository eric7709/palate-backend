package com.app.palate.customer.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.customer.Customer;
import com.app.palate.customer.CustomerRepository;
import com.app.palate.customer.dto.CustomerRequestDTO;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/**
 * Creates a customer, or updates the existing one if the phone number (preferred in a
 * restaurant) or, failing that, the email already belongs to someone.
 */
@Service
@RequiredArgsConstructor
public class CreateCustomer {
    private final CustomerRepository customerRepository;

    @Transactional
    public Customer create(CustomerRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getName(), "Name");

        Customer customer = null;

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            customer = customerRepository.findByPhoneNumber(request.getPhoneNumber().trim()).orElse(null);
        }

        if (customer == null && request.getEmail() != null && !request.getEmail().isBlank()) {
            customer = customerRepository.findByEmail(request.getEmail().trim()).orElse(null);
        }

        if (customer == null) {
            customer = new Customer();
        }

        customer.setName(request.getName().trim());

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            customer.setPhoneNumber(request.getPhoneNumber().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            customer.setEmail(request.getEmail().trim());
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            customer.setTitle(request.getTitle().trim());
        }

        return customerRepository.save(customer);
    }
}
