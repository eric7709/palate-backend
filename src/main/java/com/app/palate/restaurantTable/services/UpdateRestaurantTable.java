package com.app.palate.restaurantTable.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.AuthService;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.restaurantTable.dto.RestaurantTableRequestDTO;
import com.app.palate.tableAllocation.services.AllocateStaff;
import com.app.palate.tableAllocation.services.DeallocateStaff;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/**
 * Changes a table's name and number, and moves its waiter and cashier: a different one is
 * allocated, and leaving one out removes the current one.
 */
@Service
@RequiredArgsConstructor
public class UpdateRestaurantTable {
    private final RestaurantTableRepository restaurantTableRepository;
    private final GetRestaurantTableById getRestaurantTableById;
    private final AllocateStaff allocateStaff;
    private final DeallocateStaff deallocateStaff;
    private final AuthService authService;

    @Transactional
    public RestaurantTable update(Long id, RestaurantTableRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Table ID");
        ValidationUtils.requireNonNull(request, "Request body");
        RestaurantTable table = getRestaurantTableById.get(id);

        ValidationUtils.requireNonBlank(request.getTableName(), "Table name");
        ensureNameNotTakenByAnother(request.getTableName().trim(), id);
        table.setTableName(request.getTableName().trim());

        ValidationUtils.requireNonNull(request.getTableNumber(), "Table number");
        ensureNumberNotTakenByAnother(request.getTableNumber(), id);
        table.setTableNumber(request.getTableNumber());

        Long waiterId = request.getWaiterId();
        if (waiterId != null) {
            boolean isDifferent = table.getWaiter() == null || !table.getWaiter().getId().equals(waiterId);
            if (isDifferent) {
                allocateStaff.allocate(table.getId(), waiterId);
                table.setWaiter(authService.getAccountById(waiterId));
            }
        } else if (table.getWaiter() != null) {
            deallocateStaff.deallocate(table.getId(), table.getWaiter().getId());
            table.setWaiter(null);
        }

        Long cashierId = request.getCashierId();
        if (cashierId != null) {
            boolean isDifferent = table.getCashier() == null || !table.getCashier().getId().equals(cashierId);
            if (isDifferent) {
                allocateStaff.allocate(table.getId(), cashierId);
                table.setCashier(authService.getAccountById(cashierId));
            }
        } else if (table.getCashier() != null) {
            deallocateStaff.deallocate(table.getId(), table.getCashier().getId());
            table.setCashier(null);
        }

        if (request.getStatus() != null) table.setStatus(request.getStatus());
        if (request.getCapacity() != null) {
            ValidationUtils.requireGreaterThanZero(request.getCapacity(), "Capacity");
            table.setCapacity(request.getCapacity());
        }
        return restaurantTableRepository.save(table);
    }

    private void ensureNameNotTakenByAnother(String tableName, Long id) {
        RestaurantTable byName = restaurantTableRepository.findByTableNameContainingIgnoreCase(tableName);
        if (byName != null && !byName.getId().equals(id)) throw new BadRequestException("Table Name already exists");
    }

    private void ensureNumberNotTakenByAnother(Integer tableNumber, Long id) {
        RestaurantTable byNumber = restaurantTableRepository.findByTableNumber(tableNumber);
        if (byNumber != null && !byNumber.getId().equals(id)) throw new BadRequestException("Table Number already exists");
    }
}
