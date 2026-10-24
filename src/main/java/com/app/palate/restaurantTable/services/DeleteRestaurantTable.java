package com.app.palate.restaurantTable.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.tableAllocation.services.DeallocateStaff;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Deletes a table after releasing its waiter and cashier. */
@Service
@RequiredArgsConstructor
public class DeleteRestaurantTable {
    private final RestaurantTableRepository restaurantTableRepository;
    private final GetRestaurantTableById getRestaurantTableById;
    private final DeallocateStaff deallocateStaff;

    @Transactional
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Table ID");
        RestaurantTable table = getRestaurantTableById.get(id);

        if (table.getWaiter() != null) {
            deallocateStaff.deallocate(table.getId(), table.getWaiter().getId());
        }
        if (table.getCashier() != null) {
            deallocateStaff.deallocate(table.getId(), table.getCashier().getId());
        }
        restaurantTableRepository.deleteById(id);
    }
}
