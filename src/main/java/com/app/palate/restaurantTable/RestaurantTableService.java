package com.app.palate.restaurantTable;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.app.palate.auth.Account;
import com.app.palate.auth.AuthService;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.service.QrCodeService;
import com.app.palate.tableAllocation.TableAllocationService;
import com.app.palate.utils.ValidationUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;
    private final TableAllocationService tableAllocationService;
    private final AuthService authService;
    private final QrCodeService qrCodeService;

    // =========================
    // Create Table (FIXED)
    // =========================
    @Transactional
    public RestaurantTable createTable(RestaurantTableRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getTableName(), "Table name");
        ValidationUtils.requireNonNull(request.getTableNumber(), "Table number");

        if (request.getCapacity() != null) {
            ValidationUtils.requireGreaterThanZero(request.getCapacity(), "Capacity");
        }

        RestaurantTableStatus tableStatus = request.getStatus() != null
                ? request.getStatus()
                : RestaurantTableStatus.AVAILABLE;

        validateUniqueNameOnCreation(request.getTableName().trim());
        validateUniqueNumberOnCreation(request.getTableNumber());

        RestaurantTable table = new RestaurantTable();
        table.setTableName(request.getTableName().trim());
        table.setTableNumber(request.getTableNumber());
        table.setStatus(tableStatus);
        table.setCapacity(request.getCapacity());

        // 1. Explicitly tokenized and bound directly prior to the transaction flash write
        table.setQrCode(qrCodeService.generateRandomQrCode());

        RestaurantTable savedTable = restaurantTableRepository.save(table);

        if (request.getWaiterId() != null) {
            tableAllocationService.allocateStaff(savedTable.getId(), request.getWaiterId());
            savedTable.setWaiter(authService.getAccountById(request.getWaiterId()));
        }

        if (request.getCashierId() != null) {
            tableAllocationService.allocateStaff(savedTable.getId(), request.getCashierId());
            savedTable.setCashier(authService.getAccountById(request.getCashierId()));
        }

        // FIX: Return the fully built memory transaction entity safely to preserve the QR context
        return savedTable;
    }

    // =========================
    // Bulk Create Tables (FIXED)
    // =========================
    @Transactional
    public List<RestaurantTable> createTablesBulk(List<RestaurantTableRequestDTO> requests) {
        ValidationUtils.requireNotEmpty(requests, "Bulk request list");

        List<RestaurantTable> tablesToSave = new ArrayList<>();
        java.util.Set<String> incomingNamesLower = new java.util.HashSet<>();
        java.util.Set<Integer> incomingNumbers = new java.util.HashSet<>();

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
            validateUniqueNameOnCreation(req.getTableName().trim());
            validateUniqueNumberOnCreation(req.getTableNumber());

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
                tableAllocationService.allocateStaff(savedTable.getId(), req.getWaiterId());
                savedTable.setWaiter(authService.getAccountById(req.getWaiterId()));
            }
            if (req.getCashierId() != null) {
                tableAllocationService.allocateStaff(savedTable.getId(), req.getCashierId());
                savedTable.setCashier(authService.getAccountById(req.getCashierId()));
            }
        }

        // FIX: Directly return the hydrated array block cleanly without running database cache-flushing re-select loops
        return savedTables;
    }

    // Keep the rest of your unchanged methods (getTableById, getAllTables, updateTable, etc.) safely below...
    public RestaurantTable getTableById(Long id) {
        ValidationUtils.requireNonNull(id, "Table ID");
        return restaurantTableRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Table not found"));
    }

    public List<RestaurantTable> getTablesByAccount(Long waiterId, Long cashierId) {
        if (waiterId != null) return restaurantTableRepository.findByWaiterId(waiterId);
        if (cashierId != null) return restaurantTableRepository.findByCashierId(cashierId);
        return null;
    }

    public Page<RestaurantTable> getAllTables(String search, String status, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<RestaurantTable> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Join<RestaurantTable, Account> waiterJoin = root.join("waiter", JoinType.LEFT);
                Join<RestaurantTable, Account> cashierJoin = root.join("cashier", JoinType.LEFT);
                Predicate byTableName = cb.like(cb.lower(root.get("tableName")), pattern);
                Predicate byTableNumber = cb.like(cb.toString(root.get("tableNumber")), pattern);
                Predicate byWaiterFirst = cb.like(cb.lower(waiterJoin.get("firstName")), pattern);
                Predicate byWaiterLast = cb.like(cb.lower(waiterJoin.get("lastName")), pattern);
                Predicate byCashierFirst = cb.like(cb.lower(cashierJoin.get("firstName")), pattern);
                Predicate byCashierLast = cb.like(cb.lower(cashierJoin.get("lastName")), pattern);
                predicates.add(cb.or(byTableName, byTableNumber, byWaiterFirst, byWaiterLast, byCashierFirst, byCashierLast));
                query.distinct(true);
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), RestaurantTableStatus.valueOf(status)));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
        return restaurantTableRepository.findAll(spec, pageable);
    }

    @Transactional
    public void deleteTable(Long id) {
        ValidationUtils.requireNonNull(id, "Table ID");
        RestaurantTable table = getTableById(id);
        if (table.getWaiter() != null) {
            tableAllocationService.deallocateStaff(table.getId(), table.getWaiter().getId());
        }
        if (table.getCashier() != null) {
            tableAllocationService.deallocateStaff(table.getId(), table.getCashier().getId());
        }
        restaurantTableRepository.deleteById(id);
    }

    
    
    @Transactional
    public RestaurantTable updateTable(Long id, RestaurantTableRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Table ID");
        ValidationUtils.requireNonNull(request, "Request body");
        RestaurantTable table = getTableById(id);
        ValidationUtils.requireNonBlank(request.getTableName(), "Table name");
        validateUniqueNameOnUpdate(request.getTableName().trim(), id);
        table.setTableName(request.getTableName().trim());
        ValidationUtils.requireNonNull(request.getTableNumber(), "Table number");
        validateUniqueNumberOnUpdate(request.getTableNumber(), id);
        table.setTableNumber(request.getTableNumber());

        Long waiterId = request.getWaiterId();
        if (waiterId != null) {
            boolean isDifferent = table.getWaiter() == null || !table.getWaiter().getId().equals(waiterId);
            if (isDifferent) {
                tableAllocationService.allocateStaff(table.getId(), waiterId);
                table.setWaiter(authService.getAccountById(waiterId));
            }
        } else if (table.getWaiter() != null) {
            tableAllocationService.deallocateStaff(table.getId(), table.getWaiter().getId());
            table.setWaiter(null);
        }

        Long cashierId = request.getCashierId();
        if (cashierId != null) {
            boolean isDifferent = table.getCashier() == null || !table.getCashier().getId().equals(cashierId);
            if (isDifferent) {
                tableAllocationService.allocateStaff(table.getId(), cashierId);
                table.setCashier(authService.getAccountById(cashierId));
            }
        } else if (table.getCashier() != null) {
            tableAllocationService.deallocateStaff(table.getId(), table.getCashier().getId());
            table.setCashier(null);
        }

        if (request.getStatus() != null) table.setStatus(request.getStatus());
        if (request.getCapacity() != null) {
            ValidationUtils.requireGreaterThanZero(request.getCapacity(), "Capacity");
            table.setCapacity(request.getCapacity());
        }
        return restaurantTableRepository.save(table);
    }

    public RestaurantTable getTableByQrCode(String qrCode) {
        ValidationUtils.requireNonBlank(qrCode, "QR Code token");
        return restaurantTableRepository.findByQrCode(qrCode.trim())
                .orElseThrow(() -> new BadRequestException("Invalid or unrecognized QR code token"));
    }

    private void validateUniqueNameOnCreation(String tableName) {
        RestaurantTable byName = restaurantTableRepository.findByTableNameContainingIgnoreCase(tableName);
        if (byName != null) throw new BadRequestException("Table Name already exists");
    }

    private void validateUniqueNumberOnCreation(Integer tableNumber) {
        RestaurantTable byNumber = restaurantTableRepository.findByTableNumber(tableNumber);
        if (byNumber != null) throw new BadRequestException("Table Number already exists");
    }

    private void validateUniqueNameOnUpdate(String tableName, Long id) {
        RestaurantTable byName = restaurantTableRepository.findByTableNameContainingIgnoreCase(tableName);
        if (byName != null && !byName.getId().equals(id)) throw new BadRequestException("Table Name already exists");
    }

    private void validateUniqueNumberOnUpdate(Integer tableNumber, Long id) {
        RestaurantTable byNumber = restaurantTableRepository.findByTableNumber(tableNumber);
        if (byNumber != null && !byNumber.getId().equals(id)) throw new BadRequestException("Table Number already exists");
    }
}