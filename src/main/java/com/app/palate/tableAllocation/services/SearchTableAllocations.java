package com.app.palate.tableAllocation.services;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.tableAllocation.TableAllocation;
import com.app.palate.tableAllocation.TableAllocationRepository;
import com.app.palate.tableAllocation.dto.TableAllocationSearchCriteria;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

/**
 * Lists allocations one page at a time. The search text is matched against the table and the staff
 * on it; table, staff, role, active/inactive and day narrow the results further.
 */
@Service
@RequiredArgsConstructor
public class SearchTableAllocations {
    private final TableAllocationRepository tableAllocationRepository;

    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public Page<TableAllocation> search(TableAllocationSearchCriteria criteria) {
        Sort sort = criteria.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(criteria.getSortBy()).descending()
                : Sort.by(criteria.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        return tableAllocationRepository.findAll(matching(criteria), pageable);
    }

    private Specification<TableAllocation> matching(TableAllocationSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                String pattern = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                var tableJoin = root.join("table", JoinType.LEFT);
                var cashierJoin = root.join("cashier", JoinType.LEFT);
                var waiterJoin = root.join("waiter", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(root.get("id").as(String.class), pattern),
                        cb.like(cb.lower(tableJoin.get("tableName")), pattern),
                        cb.like(tableJoin.get("tableNumber").as(String.class), pattern),
                        cb.like(cb.lower(cashierJoin.get("firstName")), pattern),
                        cb.like(cb.lower(cashierJoin.get("lastName")), pattern),
                        cb.like(cb.lower(cashierJoin.get("email")), pattern),
                        cb.like(cashierJoin.get("phoneNumber"), pattern),
                        cb.like(cb.lower(waiterJoin.get("firstName")), pattern),
                        cb.like(cb.lower(waiterJoin.get("lastName")), pattern),
                        cb.like(cb.lower(waiterJoin.get("email")), pattern),
                        cb.like(waiterJoin.get("phoneNumber"), pattern),
                        cb.like(root.get("cashierAllocatedAt").as(String.class), pattern),
                        cb.like(root.get("cashierDeallocatedAt").as(String.class), pattern),
                        cb.like(root.get("waiterAllocatedAt").as(String.class), pattern),
                        cb.like(root.get("waiterDeallocatedAt").as(String.class), pattern),
                        cb.like(root.get("createdAt").as(String.class), pattern),
                        cb.like(root.get("updatedAt").as(String.class), pattern)));
                query.distinct(true);
            }

            if (criteria.getTableId() != null) {
                predicates.add(cb.equal(root.get("table").get("id"), criteria.getTableId()));
            }

            if (criteria.getStaffId() != null) {
                predicates.add(cb.or(
                        cb.equal(root.join("cashier", JoinType.LEFT).get("id"), criteria.getStaffId()),
                        cb.equal(root.join("waiter", JoinType.LEFT).get("id"), criteria.getStaffId())));
            }

            if (criteria.getRole() != null && !criteria.getRole().isBlank()) {
                String normalized = criteria.getRole().trim().toUpperCase();
                if (normalized.equals("ROLE_CASHIER") || normalized.equals("CASHIER")) {
                    predicates.add(cb.isNotNull(root.get("cashier")));
                } else if (normalized.equals("ROLE_WAITER") || normalized.equals("WAITER")) {
                    predicates.add(cb.isNotNull(root.get("waiter")));
                }
            }

            if (criteria.getActive() != null) {
                if (criteria.getActive()) {
                    // Active = at least one staff assigned and not yet deallocated
                    predicates.add(cb.or(
                            cb.and(
                                    cb.isNotNull(root.get("cashier")),
                                    cb.isNull(root.get("cashierDeallocatedAt"))),
                            cb.and(
                                    cb.isNotNull(root.get("waiter")),
                                    cb.isNull(root.get("waiterDeallocatedAt")))));
                } else {
                    // Inactive = every assigned slot has been closed
                    Predicate cashierClosed = cb.or(
                            cb.isNull(root.get("cashier")),
                            cb.isNotNull(root.get("cashierDeallocatedAt")));
                    Predicate waiterClosed = cb.or(
                            cb.isNull(root.get("waiter")),
                            cb.isNotNull(root.get("waiterDeallocatedAt")));
                    predicates.add(cb.and(cashierClosed, waiterClosed));
                }
            }

            if (criteria.getDate() != null) {
                Instant startOfDay = criteria.getDate().atStartOfDay().toInstant(ZoneOffset.UTC);
                Instant endOfDay = criteria.getDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                predicates.add(cb.or(
                        cb.between(root.get("cashierAllocatedAt"), startOfDay, endOfDay),
                        cb.between(root.get("waiterAllocatedAt"), startOfDay, endOfDay)));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
