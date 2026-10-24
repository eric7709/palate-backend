package com.app.palate.tableAllocation.services;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.Account;
import com.app.palate.auth.AuthService;
import com.app.palate.auth.Role;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.tableAllocation.TableAllocation;
import com.app.palate.tableAllocation.TableAllocationRepository;
import com.app.palate.tableAllocation.utils.TableAllocationCloser;
import com.app.palate.utils.EntityResolver;

import lombok.RequiredArgsConstructor;

/**
 * Puts a cashier or waiter on a table. The member's role decides which slot they take; whoever held
 * that slot before is closed off. A cashier and a waiter on the same table share one allocation record.
 */
@Service
@RequiredArgsConstructor
public class AllocateStaff {
    private final EntityResolver entityResolver;
    private final AuthService authService;
    private final TableAllocationRepository tableAllocationRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final TableAllocationCloser tableAllocationCloser;

    @Transactional
    public TableAllocation allocate(Long tableId, Long staffId) {
        if (tableId == null || staffId == null)
            throw new BadRequestException("IDs must not be null");

        RestaurantTable table = entityResolver.resolveRestaurantTable(tableId);
        Account staff = authService.getAccountById(staffId);
        Role role = staff.getRole();

        if (role == Role.ROLE_CASHIER) {
            tableAllocationCloser.closeCashier(tableId);

            TableAllocation alloc = getOrCreateOpenAllocation(tableId, table);
            alloc.setCashier(staff);
            alloc.setCashierAllocatedAt(Instant.now());

            table.setCashier(staff);
            restaurantTableRepository.save(table);
            return tableAllocationRepository.save(alloc);

        } else if (role == Role.ROLE_WAITER) {
            tableAllocationCloser.closeWaiter(tableId);

            TableAllocation alloc = getOrCreateOpenAllocation(tableId, table);
            alloc.setWaiter(staff);
            alloc.setWaiterAllocatedAt(Instant.now());

            table.setWaiter(staff);
            restaurantTableRepository.save(table);
            return tableAllocationRepository.save(alloc);

        } else {
            throw new BadRequestException("Account is not a cashier or waiter");
        }
    }

    /** The table's open allocation if there is one (so cashier and waiter share a row), otherwise a new one. */
    private TableAllocation getOrCreateOpenAllocation(Long tableId, RestaurantTable table) {
        return tableAllocationRepository.findOpenAllocationByTableId(tableId)
                .orElseGet(() -> {
                    TableAllocation fresh = new TableAllocation();
                    fresh.setTable(table);
                    return fresh;
                });
    }
}
