package com.app.palate.seed;

import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.restaurantTable.RestaurantTableStatus;
import com.app.palate.room.Room;
import com.app.palate.room.RoomRepository;
import com.app.palate.room.RoomStatus;
import com.app.palate.service.QrCodeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class RestaurantTableDataInitializer {

    private final RestaurantTableRepository tableRepository;
    private final RoomRepository roomRepository;
    private final QrCodeService qrCodeService;

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

    private static final List<RoomSeed> ROOM_SEEDS = List.of(
            new RoomSeed("101", 1),
            new RoomSeed("102", 1),
            new RoomSeed("103", 1),
            new RoomSeed("201", 2),
            new RoomSeed("202", 2),
            new RoomSeed("203", 2),
            new RoomSeed("301", 3),
            new RoomSeed("302", 3)
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
                        table.setQrCode(qrCodeService.generateRandomQrCode());
                        table.setCreatedAt(now);
                        table.setUpdatedAt(now);
                        return table;
                    })
                    .toList();

            if (!toSave.isEmpty()) {
                tableRepository.saveAll(toSave);

                StringBuilder summary = new StringBuilder();
                toSave.forEach(t -> summary.append(String.format(
                        "\n   -> " + Ansi.CYAN + "[No. %d]" + Ansi.RESET + " %s (Cap: %d) | QR: " + Ansi.YELLOW + "%s" + Ansi.RESET, 
                        t.getTableNumber(), t.getTableName(), t.getCapacity(), t.getQrCode())));

                log.info("""
                    
                    """ + Ansi.GREEN + """
                    ================================================================
                    🌱 DATABASE SEEDER: TABLES GENERATED
                    Successfully persisted {} new dining tables to inventory:{}
                    ================================================================
                    """ + Ansi.RESET + """
                    """, toSave.size(), summary.toString());
            } else {
                log.info("ℹ️ Table seeding skipped: Records already match target data.");
            }
        };
    }

    @Bean
    @Order(7)
    CommandLineRunner seedRooms() {
        return args -> {
            Set<String> existingRoomNumbers = new HashSet<>(
                    roomRepository.findExistingRoomNumbers(
                            ROOM_SEEDS.stream()
                                    .map(RoomSeed::roomNumber)
                                    .toList()
                    )
            );

            Instant now = Instant.now();

            List<Room> toSave = ROOM_SEEDS.stream()
                    .filter(seed -> !existingRoomNumbers.contains(seed.roomNumber()))
                    .map(seed -> {
                        Room room = new Room();
                        room.setRoomNumber(seed.roomNumber());
                        room.setFloor(seed.floor());
                        room.setStatus(RoomStatus.AVAILABLE);
                        room.setQrCode(qrCodeService.generateRandomQrCode());
                        room.setCreatedAt(now);
                        room.setUpdatedAt(now);
                        return room;
                    })
                    .toList();

            if (!toSave.isEmpty()) {
                roomRepository.saveAll(toSave);

                StringBuilder summary = new StringBuilder();
                toSave.forEach(r -> summary.append(String.format(
                        "\n   -> " + Ansi.CYAN + "Room %s" + Ansi.RESET + " (Floor %d) | QR: " + Ansi.YELLOW + "%s" + Ansi.RESET, 
                        r.getRoomNumber(), r.getFloor(), r.getQrCode())));

                log.info("""
                    
                    """ + Ansi.GREEN + """
                    ================================================================
                    🌱 DATABASE SEEDER: ROOMS GENERATED
                    Successfully persisted {} operational rooms to inventory:{}
                    ================================================================
                    """ + Ansi.RESET + """
                    """, toSave.size(), summary.toString());
            } else {
                log.info("ℹ️ Room seeding skipped: Records already match target data.");
            }
        };
    }

    private record TableSeed(String name, int tableNumber, int capacity) {}

    private record RoomSeed(String roomNumber, int floor) {}

    // Inline utility class to handle ANSI coloring parameters dynamically
    private static class Ansi {
        private static final String RESET = "\u001B[0m";
        private static final String GREEN = "\u001B[32m";
        private static final String CYAN = "\u001B[36m";
        private static final String YELLOW = "\u001B[33m";
    }
}