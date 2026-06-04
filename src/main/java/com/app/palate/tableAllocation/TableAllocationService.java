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

    public Page<TableAllocation> getAllAllocations(
            Long tableId,
            Long staffId,
            String role,
            Boolean active,
            LocalDate date,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<TableAllocation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tableId != null) {
                predicates.add(cb.equal(root.get("table").get("id"), tableId));
            }

            // FIX #3: use explicit LEFT JOINs to avoid null path traversal on nullable FKs
            if (staffId != null) {
                predicates.add(cb.or(
                        cb.equal(root.join("cashier", JoinType.LEFT).get("id"), staffId),
                        cb.equal(root.join("waiter", JoinType.LEFT).get("id"), staffId)));
            }

            if (role != null && !role.isBlank()) {
                String normalized = role.trim().toUpperCase();
                if (normalized.equals("ROLE_CASHIER") || normalized.equals("CASHIER")) {
                    predicates.add(cb.isNotNull(root.get("cashier")));
                } else if (normalized.equals("ROLE_WAITER") || normalized.equals("WAITER")) {
                    predicates.add(cb.isNotNull(root.get("waiter")));
                }
            }
            // FIX #5: tightened active/inactive predicate logic
            if (active != null) {
                if (active) {
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

            if (date != null) {
                Instant startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC);
                Instant endOfDay = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
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