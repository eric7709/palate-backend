package com.app.palate.seed;

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

    private static final List<TableSeed> TABLE_SEEDS = List.of(
            new TableSeed("Tokyo",    1, 2),
            new TableSeed("Paris",    2, 2),
            new TableSeed("New York", 3, 4),
            new TableSeed("London",   4, 4),
            new TableSeed("Dubai",    5, 6),
            new TableSeed("Sydney",   6, 6),
            new TableSeed("Rome",     7, 8),
            new TableSeed("Istanbul", 8, 10)
    );

    @Bean
    @Order(6)
    CommandLineRunner seedTables() {
        return args -> {
            Set<Integer> existingNumbers = new HashSet<>(
                    tableRepository.findExistingTableNumbers(
                            TABLE_SEEDS.stream()
                                    .map(TableSeed::tableNumber)
                                    .toList()
                    )
            );

            Instant now = Instant.now();

            List<RestaurantTable> toSave = TABLE_SEEDS.stream()
                    .filter(seed -> !existingNumbers.contains(seed.tableNumber()))
                    .map(seed -> {
                        RestaurantTable table = new RestaurantTable();
                        table.setTableName(seed.name());
                        table.setTableNumber(seed.tableNumber());
                        table.setCapacity(seed.capacity());
                        table.setStatus(RestaurantTableStatus.AVAILABLE);
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