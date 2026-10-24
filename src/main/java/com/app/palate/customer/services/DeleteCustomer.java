package com.app.palate.customer.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.customer.Customer;
import com.app.palate.customer.CustomerRepository;
import com.app.palate.exceptions.ResourceNotFoundException;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteCustomer {
    private final CustomerRepository customerRepository;

    @Transactional
    @SuppressWarnings("null")
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Customer ID");

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customerRepository.delete(customer);
    }
}
