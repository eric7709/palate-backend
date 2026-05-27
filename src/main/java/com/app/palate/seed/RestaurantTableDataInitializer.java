package com.app.palate.seed;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.restaurantTable.RestaurantTableStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class RestaurantTableDataInitializer {

    private final RestaurantTableRepository tableRepository;
    private final AccountRepository accountRepository;

    private static final List<TableSeed> TABLE_SEEDS = List.of(
            new TableSeed("Table A1", 1, 2),
            new TableSeed("Table A2", 2, 2),
            new TableSeed("Table B1", 3, 4),
            new TableSeed("Table B2", 4, 4),
            new TableSeed("Table C1", 5, 6),
            new TableSeed("Table C2", 6, 6),
            new TableSeed("VIP 1", 7, 8),
            new TableSeed("VIP 2", 8, 10)
    );

    @Bean
    @Order(4)
    CommandLineRunner seedTables() {
        return args -> {
            Set<Integer> existingNumbers = new HashSet<>(
                    tableRepository.findExistingTableNumbers(
                            TABLE_SEEDS.stream()
                                    .map(TableSeed::tableNumber)
                                    .toList()
                    )
            );

            List<Account> waiters = accountRepository.findByRole(com.app.palate.auth.Role.ROLE_WAITER);
            List<Account> cashiers = accountRepository.findByRole(com.app.palate.auth.Role.ROLE_CASHIER);

            Account defaultWaiter = waiters.isEmpty() ? null : waiters.get(0);
            Account defaultCashier = cashiers.isEmpty() ? null : cashiers.get(0);

            Instant now = Instant.now();

            List<RestaurantTable> toSave = TABLE_SEEDS.stream()
                    .filter(seed -> !existingNumbers.contains(seed.tableNumber()))
                    .map(seed -> {
                        RestaurantTable table = new RestaurantTable();
                        table.setTableName(seed.name());
                        table.setTableNumber(seed.tableNumber());
                        table.setCapacity(seed.capacity());
                        table.setStatus(RestaurantTableStatus.AVAILABLE);
                        table.setWaiter(defaultWaiter);
                        table.setCashier(defaultCashier);
                        
                        // FIX: Manually set audit fields to prevent NULL constraint violations
                        table.setCreatedAt(now);
                        table.setUpdatedAt(now);

                        return table;
                    })
                    .toList();

            if (!toSave.isEmpty()) {
                tableRepository.saveAll(toSave);
                System.out.println("✅ Seeded " + toSave.size() + " tables");
            } else {
                System.out.println("ℹ️ Tables already seeded");
            }
        };
    }

    private record TableSeed(String name, int tableNumber, int capacity) {}
}