package com.app.palate.restaurantTable.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.tableAllocation.services.DeallocateStaff;

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
        RestaurantTable table = getRestaurantTableById.get(id);

        if (table.getWaiter() != null) {
            deallocateStaff.deallocate(table.getId(), table.getWaiter().getId());
        }
        if (table.getCashier() != null) {
            deallocateStaff.deallocate(table.getId(), table.getCashier().getId());
        }

         if(id  == null){
            throw new BadRequestException("Table id is required");
        }
        
        restaurantTableRepository.deleteById(id);
    }
}
