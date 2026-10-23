package com.app.palate.customer.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.customer.Customer;
import com.app.palate.customer.CustomerRepository;
import com.app.palate.customer.dto.CustomerRequestDTO;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Changes the name, title and phone number of a customer. Blank fields are left as they are. */
@Service
@RequiredArgsConstructor
public class UpdateCustomer {
    private final CustomerRepository customerRepository;

    @Transactional
    public Customer update(Long id, CustomerRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Customer ID");
        ValidationUtils.requireNonNull(request, "Request body");

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Customer not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            customer.setName(request.getName().trim());
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            customer.setTitle(request.getTitle().trim());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            String phoneNumber = request.getPhoneNumber().trim();
            ensurePhoneNotTakenByAnother(phoneNumber, id);
            customer.setPhoneNumber(phoneNumber);
        }

        return customerRepository.save(customer);
    }

    private void ensurePhoneNotTakenByAnother(String phoneNumber, Long id) {
        Customer customerByPhone = customerRepository.findByPhoneNumber(phoneNumber).orElse(null);
        if (customerByPhone != null && !customerByPhone.getId().equals(id)) {
            throw new BadRequestException("Phone already exists");
        }
    }
}
