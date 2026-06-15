package com.app.palate.seed;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.auth.Role;
import com.app.palate.customer.Customer;
import com.app.palate.customer.CustomerRepository;
import com.app.palate.menuItem.MenuItem;
import com.app.palate.menuItem.MenuItemRepository;
import com.app.palate.order.Order;
import com.app.palate.order.OrderRepository;
import com.app.palate.order.OrderStatus;
import com.app.palate.orderItem.OrderItem;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.room.Room;
import com.app.palate.room.RoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class OrderDataInitializer {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final RoomRepository roomRepository;

    // The complete exact list of your 100 structural menu combinations
    private static final List<SeedRecipe> SEED_RECIPES = List.of(
            // --- Original 30 seeds ---
            new SeedRecipe(OrderStatus.PENDING, 0, -1, false, List.of("Grilled Ribeye (Imported)", "French Fries", "Caesar Salad")),
            new SeedRecipe(OrderStatus.PREPARING, 1, -1, false, List.of("Salmon Fillet", "Seasonal Vegetables")),
            new SeedRecipe(OrderStatus.COMPLETED, 2, -1, false, List.of("Classic English Breakfast", "Oats", "Custard")),
            new SeedRecipe(OrderStatus.PAID, 3, -1, false, List.of("Chicken Pizza", "Grilled Chicken Burger", "Classic Cheesecake")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("Shawarma - Chicken", "Shawarma - Beef")),
            new SeedRecipe(OrderStatus.COMPLETED, 4, -1, false, List.of("Short Ribs", "Steamed Basmati Rice", "Seasonal Vegetables")),
            new SeedRecipe(OrderStatus.CANCELLED, 5, -1, false, List.of("T-Bone Steak (Imported)", "Sweet Potato Fries")),
            new SeedRecipe(OrderStatus.PAID, -1, 0, false, List.of("Safron Signature Breakfast", "Yellow Pap")),
            new SeedRecipe(OrderStatus.COMPLETED, 6, -1, false, List.of("Seafood Platter", "Steamed Basmati Rice")),
            new SeedRecipe(OrderStatus.PAID, 7, -1, false, List.of("Spaghetti Bolognese", "Garden Salad", "Ice Cream (Classic)")),
            new SeedRecipe(OrderStatus.PAID, -1, 1, false, List.of("Nigerian Breakfast", "Custard")),
            new SeedRecipe(OrderStatus.COMPLETED, 0, -1, false, List.of("Cordon Blue", "Fried Plantain", "Special Safron Brownie")),
            new SeedRecipe(OrderStatus.PAID, 1, -1, false, List.of("Signature Platter", "The Safron Smoky Jollof")),
            new SeedRecipe(OrderStatus.CANCELLED, 2, -1, false, List.of("Prawn Thermidor", "Steamed Basmati Rice")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("Classic Club Sandwich", "French Fries")),
            new SeedRecipe(OrderStatus.COMPLETED, 3, -1, false, List.of("Roast Chicken - Full Roast", "Coleslaw", "Fried Rice (Side)")),
            new SeedRecipe(OrderStatus.PAID, 4, -1, false, List.of("Lamb Chops (Imported)", "Yam Fries", "Greek Salad")),
            new SeedRecipe(OrderStatus.COMPLETED, -1, 2, false, List.of("Peppered Snails (Igbin)", "Chicken Pepper Soup")),
            new SeedRecipe(OrderStatus.PAID, 5, -1, false, List.of("Mixed Platter", "Steamed Basmati Rice")),
            new SeedRecipe(OrderStatus.CANCELLED, 6, -1, false, List.of("Seafood Pasta", "Garden Salad")),
            new SeedRecipe(OrderStatus.PAID, -1, 3, false, List.of("Classic English Breakfast", "Waffles", "Strawberry Pancakes")),
            new SeedRecipe(OrderStatus.COMPLETED, 7, -1, false, List.of("Grilled Croaker Fillet", "The Safron Smoky Jollof", "Seasonal Vegetables")),
            new SeedRecipe(OrderStatus.PAID, 0, -1, false, List.of("Suya Pizza - Chicken", "Suya Pizza - Beef", "Extra Cheese (Pizza)")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("The Safron Special Burger", "Sweet Potato Fries", "Ice Cream (Classic)")),
            new SeedRecipe(OrderStatus.COMPLETED, 1, -1, false, List.of("Seafood Salad", "Prawns Tempura", "Classic Cheesecake")),
            new SeedRecipe(OrderStatus.CANCELLED, 2, -1, false, List.of("Alfredo - Opt Prawn", "Avocado Salad")),
            new SeedRecipe(OrderStatus.PAID, -1, 0, false, List.of("Full Breakfast Buffet", "Yellow Pap")),
            new SeedRecipe(OrderStatus.COMPLETED, 3, -1, false, List.of("Short Ribs", "Coleslaw", "Event Cake - Chocolate")),
            new SeedRecipe(OrderStatus.PAID, 4, -1, false, List.of("Mixed Platter", "Fried Rice (Side)", "Goat Meat Pepper Soup")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("Meat Pie", "Chicken Pie", "Doughnut")),

            // --- 20 seeds ---
            new SeedRecipe(OrderStatus.PAID, 5, -1, false, List.of("Prawn Thermidor", "Seasonal Vegetables", "Ice Cream (Classic)")),
            new SeedRecipe(OrderStatus.COMPLETED, 6, -1, true, List.of("Chicken Wings", "French Fries")),
            new SeedRecipe(OrderStatus.PREPARING, -1, 1, false, List.of("Safron Signature Native Fried Rice", "The Safron Smoky Jollof")),
            new SeedRecipe(OrderStatus.PENDING, 7, -1, false, List.of("Grilled Ribeye (Imported)", "French Fries")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("Shawarma - Mixed Chicken & Beef", "Doughnut")),
            new SeedRecipe(OrderStatus.COMPLETED, 0, -1, false, List.of("Seafood Okro", "Steamed Basmati Rice")),
            new SeedRecipe(OrderStatus.CANCELLED, 1, -1, false, List.of("T-Bone Steak (Imported)", "Sweet Potato Fries")),
            new SeedRecipe(OrderStatus.PAID, -1, 2, false, List.of("Safron Signature Native Fried Rice", "Turkey Sauce (Protein)")),
            new SeedRecipe(OrderStatus.PREPARING, 2, -1, false, List.of("Salmon Fillet", "Seasonal Vegetables")),
            new SeedRecipe(OrderStatus.COMPLETED, 3, -1, false, List.of("Mixed Platter", "Greek Salad")),
            new SeedRecipe(OrderStatus.PAID, 4, -1, true, List.of("Classic Club Sandwich", "Seasonal Vegetables")),
            new SeedRecipe(OrderStatus.CANCELLED, -1, 3, false, List.of("Nigerian Breakfast", "Yellow Pap")),
            new SeedRecipe(OrderStatus.PAID, 5, -1, false, List.of("Short Ribs", "Steamed Basmati Rice", "Coleslaw")),
            new SeedRecipe(OrderStatus.COMPLETED, 6, -1, false, List.of("Suya Pizza - Beef", "Extra Cheese (Pizza)")),
            new SeedRecipe(OrderStatus.PAID, 7, -1, true, List.of("The Safron Special Burger", "Yam Fries")),
            new SeedRecipe(OrderStatus.PREPARING, -1, 0, false, List.of("Peppered Snails (Igbin)", "Chicken Wings")),
            new SeedRecipe(OrderStatus.COMPLETED, 0, -1, false, List.of("Cordon Blue", "French Fries", "Event Cake - Red Velvet")),
            new SeedRecipe(OrderStatus.PAID, 1, -1, false, List.of("Seafood Pasta", "Caesar Salad")),
            new SeedRecipe(OrderStatus.CANCELLED, 2, -1, false, List.of("Prawns Tempura", "Avocado Salad")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("Chicken Pie", "Meat Pie", "Doughnut")),

            // --- 50 additional seeds ---
            new SeedRecipe(OrderStatus.PAID, 2, -1, false, List.of("Suya Pizza - Chicken", "Coleslaw")),
            new SeedRecipe(OrderStatus.PREPARING, 3, -1, false, List.of("Grilled Croaker Fillet", "Seasonal Vegetables")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("Classic Club Sandwich", "Doughnut")),
            new SeedRecipe(OrderStatus.COMPLETED, 4, -1, false, List.of("Seafood Platter", "Garden Salad", "Ice Cream (Classic)")),
            new SeedRecipe(OrderStatus.PENDING, 5, -1, false, List.of("Lamb Chops (Imported)", "Yam Fries")),
            new SeedRecipe(OrderStatus.PAID, -1, 1, false, List.of("Safron Signature Breakfast", "Custard")),
            new SeedRecipe(OrderStatus.COMPLETED, 6, -1, false, List.of("Signature Platter", "Steamed Basmati Rice", "Coleslaw")),
            new SeedRecipe(OrderStatus.CANCELLED, 7, -1, false, List.of("Alfredo - Opt Prawn", "Garden Salad")),
            new SeedRecipe(OrderStatus.PAID, 0, -1, false, List.of("Chicken Pizza", "Extra Cheese (Pizza)")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("Shawarma - Beef", "Shawarma - Chicken", "Ice Cream (Classic)")),
            new SeedRecipe(OrderStatus.COMPLETED, 1, -1, false, List.of("Roast Chicken - Full Roast", "Fried Rice (Side)")),
            new SeedRecipe(OrderStatus.PAID, 2, -1, false, List.of("Mixed Platter", "Steamed Basmati Rice", "Goat Meat Pepper Soup")),
            new SeedRecipe(OrderStatus.PREPARING, -1, 2, false, List.of("Nigerian Breakfast", "Yellow Pap")),
            new SeedRecipe(OrderStatus.COMPLETED, 3, -1, false, List.of("Salmon Fillet", "Seasonal Vegetables", "Classic Cheesecake")),
            new SeedRecipe(OrderStatus.PAID, 4, -1, false, List.of("T-Bone Steak (Imported)", "Sweet Potato Fries")),
            new SeedRecipe(OrderStatus.CANCELLED, 5, -1, false, List.of("Seafood Pasta", "Avocado Salad")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("The Safron Special Burger", "French Fries", "Doughnut")),
            new SeedRecipe(OrderStatus.COMPLETED, 6, -1, false, List.of("Short Ribs", "Steamed Basmati Rice")),
            new SeedRecipe(OrderStatus.PAID, 7, -1, false, List.of("Grilled Chicken Burger", "Sweet Potato Fries")),
            new SeedRecipe(OrderStatus.PAID, -1, 3, false, List.of("Full Breakfast Buffet", "Waffles")),
            new SeedRecipe(OrderStatus.COMPLETED, 0, -1, false, List.of("Seafood Salad", "Prawns Tempura")),
            new SeedRecipe(OrderStatus.PREPARING, 1, -1, false, List.of("Spaghetti Bolognese", "Garden Salad")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("Meat Pie", "Chicken Pie")),
            new SeedRecipe(OrderStatus.CANCELLED, 2, -1, false, List.of("Prawn Thermidor", "Seasonal Vegetables")),
            new SeedRecipe(OrderStatus.PAID, 3, -1, false, List.of("Cordon Blue", "Fried Plantain", "Special Safron Brownie")),
            new SeedRecipe(OrderStatus.COMPLETED, -1, 0, false, List.of("Safron Signature Native Fried Rice", "The Safron Smoky Jollof")),
            new SeedRecipe(OrderStatus.PAID, 4, -1, false, List.of("Suya Pizza - Beef", "Extra Cheese (Pizza)")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("Chicken Wings", "French Fries")),
            new SeedRecipe(OrderStatus.COMPLETED, 5, -1, false, List.of("Grilled Ribeye (Imported)", "Caesar Salad", "Ice Cream (Classic)")),
            new SeedRecipe(OrderStatus.PREPARING, 6, -1, false, List.of("Peppered Snails (Igbin)", "Chicken Pepper Soup")),
            new SeedRecipe(OrderStatus.PAID, 7, -1, false, List.of("Mixed Platter", "Steamed Basmati Rice")),
            new SeedRecipe(OrderStatus.COMPLETED, -1, 1, false, List.of("Classic English Breakfast", "Oats", "Custard")),
            new SeedRecipe(OrderStatus.PAID, 0, -1, false, List.of("Seafood Okro", "Steamed Basmati Rice")),
            new SeedRecipe(OrderStatus.CANCELLED, 1, -1, false, List.of("Alfredo - Opt Prawn", "Avocado Salad")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("Shawarma - Mixed Chicken & Beef", "Doughnut")),
            new SeedRecipe(OrderStatus.COMPLETED, 2, -1, false, List.of("Lamb Chops (Imported)", "Yam Fries", "Greek Salad")),
            new SeedRecipe(OrderStatus.PAID, 3, -1, false, List.of("Safron Signature Native Fried Rice", "Turkey Sauce (Protein)")),
            new SeedRecipe(OrderStatus.PREPARING, -1, 2, false, List.of("Safron Signature Breakfast", "Yellow Pap")),
            new SeedRecipe(OrderStatus.PAID, 4, -1, false, List.of("Grilled Croaker Fillet", "The Safron Smoky Jollof", "Seasonal Vegetables")),
            new SeedRecipe(OrderStatus.COMPLETED, 5, -1, false, List.of("Short Ribs", "Coleslaw", "Event Cake - Chocolate")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("Classic Club Sandwich", "French Fries", "Ice Cream (Classic)")),
            new SeedRecipe(OrderStatus.CANCELLED, 6, -1, false, List.of("Seafood Pasta", "Garden Salad")),
            new SeedRecipe(OrderStatus.PAID, 7, -1, false, List.of("Signature Platter", "The Safron Smoky Jollof")),
            new SeedRecipe(OrderStatus.COMPLETED, -1, 3, false, List.of("Nigerian Breakfast", "Custard", "Waffles")),
            new SeedRecipe(OrderStatus.PAID, 0, -1, false, List.of("Suya Pizza - Chicken", "Suya Pizza - Beef", "Extra Cheese (Pizza)")),
            new SeedRecipe(OrderStatus.PREPARING, 1, -1, false, List.of("Salmon Fillet", "Seasonal Vegetables")),
            new SeedRecipe(OrderStatus.PAID, -1, -1, true, List.of("The Safron Special Burger", "Sweet Potato Fries")),
            new SeedRecipe(OrderStatus.COMPLETED, 2, -1, false, List.of("Seafood Platter", "Steamed Basmati Rice")),
            new SeedRecipe(OrderStatus.PAID, 3, -1, false, List.of("Roast Chicken - Full Roast", "Fried Rice (Side)", "Classic Cheesecake")),
            new SeedRecipe(OrderStatus.CANCELLED, -1, 0, false, List.of("T-Bone Steak (Imported)", "Sweet Potato Fries"))
    );

    @Bean
    @org.springframework.core.annotation.Order(8)
    @Transactional
    public CommandLineRunner seedOrders() {
        return args -> {
            long existingCount = orderRepository.count();
            if (existingCount > 0) {
                log.info("ℹ️ Order seeding skipped: {} orders already exist.", existingCount);
                return;
            }

            List<Account> waiters = accountRepository.findByRole(Role.ROLE_WAITER);
            List<Account> cashiers = accountRepository.findByRole(Role.ROLE_CASHIER);
            List<Customer> customers = customerRepository.findAll();
            List<MenuItem> menuItems = menuItemRepository.findAll();
            List<RestaurantTable> tables = tableRepository.findAll();
            List<Room> rooms = roomRepository.findAll();

            if (menuItems.isEmpty()) {
                log.warn("⚠️ Order seeding skipped: No menu items found.");
                return;
            }

            ZoneId localZone = ZoneId.systemDefault();
            LocalDate today = LocalDate.now(localZone);
            
            // Calculate last Monday dynamically
            LocalDate lastMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            if (lastMonday.equals(today)) {
                lastMonday = lastMonday.minusWeeks(1);
            }

            long totalDays = ChronoUnit.DAYS.between(lastMonday, today) + 1;
            log.info("🌱 Distributing {} precise seed profiles from last Monday ({}) to today ({}) [{} Days total]", 
                    SEED_RECIPES.size(), lastMonday, today, totalDays);

            int saved = 0;
            int skippedItems = 0;

            // Mathematical baseline: spread exactly 100 profiles evenly into day chunks
            for (int i = 0; i < SEED_RECIPES.size(); i++) {
                SeedRecipe recipe = SEED_RECIPES.get(i);

                // Evenly split indices across total elapsed days
                long operationalDayOffset = i % totalDays;
                LocalDate targetDate = lastMonday.plusDays(operationalDayOffset);
                ZonedDateTime baseTime = targetDate.atStartOfDay(localZone);

                // Generate random operational rush windows based on entry index position
                int hourOffset = switch (i % 3) {
                    case 0 -> 8 + (i % 4);   // Morning window: 8 AM - 11 AM
                    case 1 -> 12 + (i % 5);  // Mid-Day window: 12 PM - 4 PM
                    default -> 17 + (i % 6); // Evening window: 5 PM - 10 PM
                };
                int minuteOffset = (i * 7) % 60;
                int secondOffset = (i * 13) % 60;

                ZonedDateTime zonedTimestamp = baseTime
                        .plusHours(hourOffset)
                        .plusMinutes(minuteOffset)
                        .plusSeconds(secondOffset);

                Instant timestamp = zonedTimestamp.toInstant();

                // Safety fallback to prevent pushing future hours on the current execution day
                if (targetDate.equals(today) && timestamp.isAfter(Instant.now())) {
                    timestamp = Instant.now().minusSeconds((SEED_RECIPES.size() - i) * 30L);
                }

                // Entity mapping
                Account waiter = waiters.isEmpty() ? null : waiters.get(i % waiters.size());
                Account cashier = cashiers.isEmpty() ? null : cashiers.get(i % cashiers.size());
                Customer customer = customers.isEmpty() ? null : customers.get(i % customers.size());

                RestaurantTable table = (recipe.tableIndex() >= 0 && recipe.tableIndex() < tables.size())
                        ? tables.get(recipe.tableIndex()) : null;
                Room room = (recipe.roomIndex() >= 0 && recipe.roomIndex() < rooms.size())
                        ? rooms.get(recipe.roomIndex()) : null;

                if (room != null) {
                    waiter = null;
                }

                Order order = new Order();
                order.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                order.setStatus(recipe.status());
                order.setWaiter(waiter);
                order.setCashier(cashier);
                order.setCustomer(customer);
                order.setTable(table);
                order.setRoom(room);
                order.setItems(new ArrayList<>());
                order.setCreatedAt(timestamp);
                order.setUpdatedAt(timestamp);

                for (String itemName : recipe.itemNames()) {
                    MenuItem menuItem = findByName(menuItems, itemName);
                    if (menuItem == null) {
                        skippedItems++;
                        continue;
                    }
                    OrderItem oi = new OrderItem();
                    oi.setOrder(order);
                    oi.setMenuItem(menuItem);
                    oi.setQuantity(1);
                    oi.setTakeOut(recipe.takeOut());
                    oi.setPrice(menuItem.getPrice());
                    order.getItems().add(oi);
                }

                if (order.getItems().isEmpty()) {
                    continue; // Skip the order record if food links fail to resolve
                }

                double total = order.getItems().stream().mapToDouble(OrderItem::getPrice).sum();
                int qty = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
                order.setTotal(total);
                order.setQuantity(qty);

                orderRepository.save(order);
                saved++;
            }

            log.info("""
                    
                    ================================================================
                    🌱 DATABASE SEEDER: COMPLETE DATA SYNCHRONIZATION FINISHED
                    Successfully saved exactly {} active orders (skipped {} bad menu references)
                    Timeline: Perfectly mapped from last Monday until today.
                    ================================================================
                    """, saved, skippedItems);
        };
    }

    private MenuItem findByName(List<MenuItem> items, String name) {
        return items.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private record SeedRecipe(
            OrderStatus status,
            int tableIndex,
            int roomIndex,
            boolean takeOut,
            List<String> itemNames) {
    }
}