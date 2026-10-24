package com.app.palate.restaurantTable.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.AuthService;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.restaurantTable.dto.RestaurantTableRequestDTO;
import com.app.palate.restaurantTable.enums.RestaurantTableStatus;
import com.app.palate.service.QrCodeService;
import com.app.palate.tableAllocation.services.AllocateStaff;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Creates a table with its own QR code, and assigns the waiter and cashier if given. */
@Service
@RequiredArgsConstructor
public class CreateRestaurantTable {
    private final RestaurantTableRepository restaurantTableRepository;
    private final AllocateStaff allocateStaff;
    private final AuthService authService;
    private final QrCodeService qrCodeService;

    @Transactional
    public RestaurantTable create(RestaurantTableRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getTableName(), "Table name");
        ValidationUtils.requireNonNull(request.getTableNumber(), "Table number");

        if (request.getCapacity() != null) {
            ValidationUtils.requireGreaterThanZero(request.getCapacity(), "Capacity");
        }

        RestaurantTableStatus tableStatus = request.getStatus() != null
                ? request.getStatus()
                : RestaurantTableStatus.AVAILABLE;

        ensureNameNotTaken(request.getTableName().trim());
        ensureNumberNotTaken(request.getTableNumber());

        RestaurantTable table = new RestaurantTable();
        table.setTableName(request.getTableName().trim());
        table.setTableNumber(request.getTableNumber());
        table.setStatus(tableStatus);
        table.setCapacity(request.getCapacity());
        table.setQrCode(qrCodeService.generateRandomQrCode());

        RestaurantTable savedTable = restaurantTableRepository.save(table);

        if (request.getWaiterId() != null) {
            allocateStaff.allocate(savedTable.getId(), request.getWaiterId());
            savedTable.setWaiter(authService.getAccountById(request.getWaiterId()));
        }

        if (request.getCashierId() != null) {
            allocateStaff.allocate(savedTable.getId(), request.getCashierId());
            savedTable.setCashier(authService.getAccountById(request.getCashierId()));
        }

        return savedTable;
    }

    private void ensureNameNotTaken(String tableName) {
        RestaurantTable byName = restaurantTableRepository.findByTableNameContainingIgnoreCase(tableName);
        if (byName != null) throw new BadRequestException("Table Name already exists");
    }

    private void ensureNumberNotTaken(Integer tableNumber) {
        RestaurantTable byNumber = restaurantTableRepository.findByTableNumber(tableNumber);
        if (byNumber != null) throw new BadRequestException("Table Number already exists");
    }
}
