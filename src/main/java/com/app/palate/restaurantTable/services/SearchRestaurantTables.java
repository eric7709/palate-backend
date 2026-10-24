package com.app.palate.restaurantTable.services;

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
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.restaurantTable.dto.RestaurantTableSearchCriteria;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

/**
 * Lists tables one page at a time. The search text is matched against the table's own fields and
 * its waiter's and cashier's details; status, number, capacity, waiter and cashier narrow it further.
 */
@Service
@RequiredArgsConstructor
public class SearchRestaurantTables {
    private final RestaurantTableRepository restaurantTableRepository;

    @Transactional(readOnly = true)
    public Page<RestaurantTable> search(RestaurantTableSearchCriteria criteria) {
        Sort sort = criteria.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(criteria.getSortBy()).descending()
                : Sort.by(criteria.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        return restaurantTableRepository.findAll(matching(criteria), pageable);
    }

    private Specification<RestaurantTable> matching(RestaurantTableSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                Join<RestaurantTable, Account> waiterJoin = root.join("waiter", JoinType.LEFT);
                Join<RestaurantTable, Account> cashierJoin = root.join("cashier", JoinType.LEFT);
                Predicate byTableName = cb.like(cb.lower(root.get("tableName")), pattern);
                Predicate byTableNumber = cb.like(cb.toString(root.get("tableNumber")), pattern);
                Predicate byCapacity = cb.like(cb.toString(root.get("capacity")), pattern);
                Predicate byQrCode = cb.like(cb.lower(root.get("qrCode")), pattern);
                Predicate byStatus = cb.like(cb.lower(root.get("status").as(String.class)), pattern);
                Predicate byId = cb.like(root.get("id").as(String.class), pattern);
                Predicate byCreatedAt = cb.like(root.get("createdAt").as(String.class), pattern);
                Predicate byUpdatedAt = cb.like(root.get("updatedAt").as(String.class), pattern);
                Predicate byWaiterId = cb.like(waiterJoin.get("id").as(String.class), pattern);
                Predicate byWaiterFirst = cb.like(cb.lower(waiterJoin.get("firstName")), pattern);
                Predicate byWaiterLast = cb.like(cb.lower(waiterJoin.get("lastName")), pattern);
                Predicate byWaiterEmail = cb.like(cb.lower(waiterJoin.get("email")), pattern);
                Predicate byWaiterPhone = cb.like(waiterJoin.get("phoneNumber"), pattern);
                Predicate byCashierFirst = cb.like(cb.lower(cashierJoin.get("firstName")), pattern);
                Predicate byCashierLast = cb.like(cb.lower(cashierJoin.get("lastName")), pattern);
                Predicate byCashierEmail = cb.like(cb.lower(cashierJoin.get("email")), pattern);
                Predicate byCashierPhone = cb.like(cashierJoin.get("phoneNumber"), pattern);
                Predicate byCashierId = cb.like(cashierJoin.get("id").as(String.class), pattern);
                predicates.add(cb.or(byTableName, byTableNumber, byCapacity, byQrCode, byStatus,
                        byId, byCreatedAt, byUpdatedAt, byWaiterId, byCashierId,
                        byWaiterFirst, byWaiterLast, byWaiterEmail, byWaiterPhone,
                        byCashierFirst, byCashierLast, byCashierEmail, byCashierPhone));
                query.distinct(true);
            }
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getTableNumber() != null) {
                predicates.add(cb.equal(root.get("tableNumber"), criteria.getTableNumber()));
            }
            if (criteria.getCapacity() != null) {
                predicates.add(cb.equal(root.get("capacity"), criteria.getCapacity()));
            }
            if (criteria.getWaiterId() != null) {
                predicates.add(cb.equal(root.get("waiter").get("id"), criteria.getWaiterId()));
            }
            if (criteria.getCashierId() != null) {
                predicates.add(cb.equal(root.get("cashier").get("id"), criteria.getCashierId()));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
