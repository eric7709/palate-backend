package com.app.palate.restaurantTable.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

/** Creates many tables at once. If any one is invalid or already exists, none are saved. */
@Service
@RequiredArgsConstructor
public class CreateRestaurantTablesBulk {
    private final RestaurantTableRepository restaurantTableRepository;
    private final AllocateStaff allocateStaff;
    private final AuthService authService;
    private final QrCodeService qrCodeService;

    @Transactional
    public List<RestaurantTable> create(List<RestaurantTableRequestDTO> requests) {
        ValidationUtils.requireNotEmpty(requests, "Bulk request list");

        List<RestaurantTable> tablesToSave = new ArrayList<>();
        Set<String> incomingNamesLower = new HashSet<>();
        Set<Integer> incomingNumbers = new HashSet<>();

        for (RestaurantTableRequestDTO req : requests) {
            ValidationUtils.requireNonNull(req, "Bulk item");
            ValidationUtils.requireNonBlank(req.getTableName(), "Table name");
            ValidationUtils.requireNonNull(req.getTableNumber(), "Table number");

            if (req.getCapacity() != null) {
                ValidationUtils.requireGreaterThanZero(req.getCapacity(), "Capacity");
            }

            String normalizedName = req.getTableName().trim().toLowerCase();
            if (!incomingNamesLower.add(normalizedName)) {
                throw new BadRequestException("Duplicate table name within request: " + req.getTableName().trim());
            }

            if (!incomingNumbers.add(req.getTableNumber())) {
                throw new BadRequestException("Duplicate table number within request: " + req.getTableNumber());
            }
        }

        for (RestaurantTableRequestDTO req : requests) {
            ensureNameNotTaken(req.getTableName().trim());
            ensureNumberNotTaken(req.getTableNumber());

            RestaurantTable table = new RestaurantTable();
            table.setTableName(req.getTableName().trim());
            table.setTableNumber(req.getTableNumber());
            table.setStatus(req.getStatus() != null ? req.getStatus() : RestaurantTableStatus.AVAILABLE);
            table.setCapacity(req.getCapacity());
            table.setQrCode(qrCodeService.generateRandomQrCode());

            tablesToSave.add(table);
        }

        List<RestaurantTable> savedTables = restaurantTableRepository.saveAll(tablesToSave);

        for (int i = 0; i < savedTables.size(); i++) {
            RestaurantTable savedTable = savedTables.get(i);
            RestaurantTableRequestDTO req = requests.get(i);

            if (req.getWaiterId() != null) {
                allocateStaff.allocate(savedTable.getId(), req.getWaiterId());
                savedTable.setWaiter(authService.getAccountById(req.getWaiterId()));
            }
            if (req.getCashierId() != null) {
                allocateStaff.allocate(savedTable.getId(), req.getCashierId());
                savedTable.setCashier(authService.getAccountById(req.getCashierId()));
            }
        }

        return savedTables;
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
