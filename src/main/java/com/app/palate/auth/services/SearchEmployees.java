package com.app.palate.auth.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.auth.dto.EmployeeSearchCriteria;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

/**
 * Lists employees one page at a time. The search text is matched against the
 * account's details;
 * role, status and gender narrow the results further. Sorted newest first
 * unless the caller asks for ascending.
 */
@Service
@RequiredArgsConstructor
public class SearchEmployees {
    private final AccountRepository accountRepository;

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public Page<Account> search(EmployeeSearchCriteria criteria) {
        String sortByField = (criteria.getSortBy() != null && !criteria.getSortBy().isEmpty())
                ? criteria.getSortBy()
                : "id";
        boolean ascending = criteria.getSortDirection() != null
                && criteria.getSortDirection().equalsIgnoreCase("ASC");

        Sort sort = ascending ? Sort.by(sortByField).ascending() : Sort.by(sortByField).descending();
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        return accountRepository.findAll(matching(criteria), pageable);
    }

    private Specification<Account> matching(EmployeeSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String cleanSearch = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), cleanSearch),
                        cb.like(cb.lower(root.get("lastName")), cleanSearch),
                        cb.like(cb.lower(root.get("email")), cleanSearch),
                        cb.like(root.get("phoneNumber"), cleanSearch),
                        cb.like(cb.lower(root.get("gender").as(String.class)), cleanSearch),
                        cb.like(cb.lower(root.get("role").as(String.class)), cleanSearch),
                        cb.like(cb.lower(root.get("status").as(String.class)), cleanSearch),
                        cb.like(root.get("id").as(String.class), cleanSearch),
                        cb.like(root.get("createdAt").as(String.class), cleanSearch),
                        cb.like(root.get("updatedAt").as(String.class), cleanSearch)));
            }

            if (criteria.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), criteria.getRole()));
            }
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getGender() != null) {
                predicates.add(cb.equal(root.get("gender"), criteria.getGender()));
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
