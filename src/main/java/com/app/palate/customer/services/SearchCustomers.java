package com.app.palate.customer.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.customer.Customer;
import com.app.palate.customer.CustomerRepository;
import com.app.palate.customer.dto.CustomerSearchCriteria;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

/**
 * Lists customers one page at a time. The search text is matched (case-insensitively)
 * against name, phone number and email; with no search text every customer is returned.
 */
@Service
@RequiredArgsConstructor
public class SearchCustomers {
    private final CustomerRepository customerRepository;

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public Page<Customer> search(CustomerSearchCriteria criteria) {
        Sort sort = criteria.getSortDirection().equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(criteria.getSortBy()).descending()
                : Sort.by(criteria.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        return customerRepository.findAll(matching(criteria.getSearch()), pageable);
    }

    private Specification<Customer> matching(String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String cleanSearch = "%" + search.trim().toLowerCase() + "%";

                Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), cleanSearch);
                Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), cleanSearch);
                Predicate phoneMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), cleanSearch);
                Predicate emailMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), cleanSearch);
                Predicate idMatch = criteriaBuilder.like(root.get("id").as(String.class), cleanSearch);
                Predicate createdAtMatch = criteriaBuilder.like(root.get("createdAt").as(String.class), cleanSearch);
                Predicate updatedAtMatch = criteriaBuilder.like(root.get("updatedAt").as(String.class), cleanSearch);

                predicates.add(criteriaBuilder.or(nameMatch, titleMatch, phoneMatch, emailMatch,
                        idMatch, createdAtMatch, updatedAtMatch));
            }

            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
