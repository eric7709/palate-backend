package com.app.palate.tableAllocation.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.Account;
import com.app.palate.auth.AuthService;
import com.app.palate.auth.Role;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.tableAllocation.utils.TableAllocationCloser;
import com.app.palate.utils.EntityResolver;

import lombok.RequiredArgsConstructor;

/** Takes a cashier or waiter off a table: closes the allocation and clears the table's assignment. */
@Service
@RequiredArgsConstructor
public class DeallocateStaff {
    private final EntityResolver entityResolver;
    private final AuthService authService;
    private final RestaurantTableRepository restaurantTableRepository;
    private final TableAllocationCloser tableAllocationCloser;

    @Transactional
    public void deallocate(Long tableId, Long staffId) {
        if (tableId == null || staffId == null)
            throw new BadRequestException("IDs must not be null");

        Account staff = authService.getAccountById(staffId);
        Role role = staff.getRole();
        RestaurantTable table = entityResolver.resolveRestaurantTable(tableId);

        if (role == Role.ROLE_CASHIER) {
            tableAllocationCloser.closeCashier(tableId);
            table.setCashier(null);

        } else if (role == Role.ROLE_WAITER) {
            tableAllocationCloser.closeWaiter(tableId);
            table.setWaiter(null);

        } else {
            throw new BadRequestException("Account is not a cashier or waiter");
        }

        restaurantTableRepository.save(table);
    }
}
