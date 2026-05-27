package com.app.palate.customer;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.exceptions.ResourceNotFoundException;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    // CREATE
    public Customer createCustomer(CustomerRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getName(), "Name");

        Customer customer = null;

        // 1. Try find by phone (preferred in restaurants)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            customer = customerRepository.findByPhoneNumber(request.getPhoneNumber().trim());
        }

        // 2. Fallback to email (optional)
        if (customer == null && request.getEmail() != null && !request.getEmail().isBlank()) {
            customer = customerRepository.findByEmail(request.getEmail().trim());
        }

        // 3. Create new customer if none found
        if (customer == null) {
            customer = new Customer();
        }

        // 4. Update fields
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

    // UPDATE
    public Customer updateCustomer(Long id, CustomerRequestDTO request) {
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
            validateUniquePhoneOnUpdate(request.getPhoneNumber().trim(), id);
            customer.setPhoneNumber(request.getPhoneNumber().trim());
        }

        return customerRepository.save(customer);
    }

    // GET ALL CUSTOMERS
    public Page<Customer> getAllCustomers(
            String search,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        // 1. Build dynamic sort routing safely
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. Compute dynamic search predicates using criteria API contract cleanly
        Specification<Customer> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String cleanSearch = "%" + search.trim().toLowerCase() + "%";

                // Match search term against name, phone, or email blocks case-insensitively
                Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), cleanSearch);
                Predicate phoneMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")),
                        cleanSearch);
                Predicate emailMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), cleanSearch);

                predicates.add(criteriaBuilder.or(nameMatch, phoneMatch, emailMatch));
            }

            // Fixes error: Returns a clean 1=1 placeholder if list is empty, satisfying the
            // lambda type compiler
            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return customerRepository.findAll(spec, pageable);
    }

    public Customer getCustomerById(Long id) {
        ValidationUtils.requireNonNull(id, "Customer ID");

        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    // DELETE
    public void deleteCustomer(Long id) {
        ValidationUtils.requireNonNull(id, "Customer ID");

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customerRepository.delete(customer);
    }

    // --- Private Business Custom Assertions ---

    private void validateUniquePhoneOnUpdate(String phoneNumber, Long id) {
        Customer customerByPhone = customerRepository.findByPhoneNumber(phoneNumber);
        if (customerByPhone != null && !customerByPhone.getId().equals(id)) {
            throw new BadRequestException("Phone already exists");
        }
    }
}