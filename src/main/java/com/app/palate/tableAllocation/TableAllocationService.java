package com.app.palate.tableAllocation;

import com.app.palate.auth.Account;
import com.app.palate.auth.AuthService;
import com.app.palate.auth.Role;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.utils.EntityResolver;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TableAllocationService {

    private final EntityResolver entityResolver;
    private final AuthService authService;
    private final TableAllocationRepository tableAllocationRepository;
    private final RestaurantTableRepository restaurantTableRepository; // FIX #2: needed to persist table changes

    // -------------------------------------------------------------------------
    // Allocate
    // -------------------------------------------------------------------------

    @Transactional
    public TableAllocation allocateStaff(Long tableId, Long staffId) {
        if (tableId == null || staffId == null)
            throw new BadRequestException("IDs must not be null");

        RestaurantTable table = entityResolver.resolveRestaurantTable(tableId);
        Account staff = authService.getAccountById(staffId);
        Role role = staff.getRole();

        if (role == Role.ROLE_CASHIER) {
            // FIX #1: close any open cashier allocation first
            closePreviousCashierAllocation(tableId);

            // FIX #1: reuse the open allocation for this table if one already exists
            // (e.g. a waiter was allocated earlier), otherwise create a new record
            TableAllocation alloc = getOrCreateOpenAllocation(tableId, table);
            alloc.setCashier(staff);
            alloc.setCashierAllocatedAt(Instant.now());

            table.setCashier(staff);
            restaurantTableRepository.save(table); // FIX #2: persist table change
            return tableAllocationRepository.save(alloc);

        } else if (role == Role.ROLE_WAITER) {
            // FIX #1: close any open waiter allocation first
            closePreviousWaiterAllocation(tableId);

            TableAllocation alloc = getOrCreateOpenAllocation(tableId, table);
            alloc.setWaiter(staff);
            alloc.setWaiterAllocatedAt(Instant.now());

            table.setWaiter(staff);
            restaurantTableRepository.save(table); // FIX #2: persist table change
            return tableAllocationRepository.save(alloc);

        } else {
            throw new BadRequestException("Account is not a cashier or waiter");
        }
    }

    // -------------------------------------------------------------------------
    // Deallocate
    // -------------------------------------------------------------------------

    @Transactional
    public void deallocateStaff(Long tableId, Long staffId) {
        if (tableId == null || staffId == null)
            throw new BadRequestException("IDs must not be null");

        Account staff = authService.getAccountById(staffId);
        Role role = staff.getRole();
        RestaurantTable table = entityResolver.resolveRestaurantTable(tableId);

        if (role == Role.ROLE_CASHIER) {
            closePreviousCashierAllocation(tableId);
            table.setCashier(null);

        } else if (role == Role.ROLE_WAITER) {
            closePreviousWaiterAllocation(tableId);
            table.setWaiter(null);

        } else {
            throw new BadRequestException("Account is not a cashier or waiter");
        }

        restaurantTableRepository.save(table); // FIX #2: persist the null assignment
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    public Page<TableAllocation> getAllAllocations(TableAllocationSearchCriteria criteria) {

        Sort sort = criteria.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(criteria.getSortBy()).descending()
                : Sort.by(criteria.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        Specification<TableAllocation> spec = (root, query, cb) -> {
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

            // FIX #3: use explicit LEFT JOINs to avoid null path traversal on nullable FKs
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
            // FIX #5: tightened active/inactive predicate logic
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
                    // A record is fully inactive when:
                    // - cashier is either absent or deallocated AND
                    // - waiter is either absent or deallocated
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

        return tableAllocationRepository.findAll(spec, pageable);
    }

    public TableAllocation getAllocationById(Long id) {
        return tableAllocationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Allocation not found"));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * FIX #1: Returns the single open allocation record for this table if one
     * exists (so cashier and waiter share one row), or creates a fresh one.
     * "Open" means at least one slot is still active (not fully deallocated).
     */
    private TableAllocation getOrCreateOpenAllocation(Long tableId, RestaurantTable table) {
        return tableAllocationRepository.findOpenAllocationByTableId(tableId)
                .orElseGet(() -> {
                    TableAllocation fresh = new TableAllocation();
                    fresh.setTable(table);
                    return fresh;
                });
    }

    private void closePreviousCashierAllocation(Long tableId) {
        tableAllocationRepository
                .findByTableIdAndCashierIsNotNullAndCashierDeallocatedAtIsNull(tableId)
                .forEach(alloc -> alloc.setCashierDeallocatedAt(Instant.now()));
    }

    private void closePreviousWaiterAllocation(Long tableId) {
        tableAllocationRepository
                .findByTableIdAndWaiterIsNotNullAndWaiterDeallocatedAtIsNull(tableId)
                .forEach(alloc -> alloc.setWaiterDeallocatedAt(Instant.now()));
    }
}