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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    private static final List<OrderSeed> ORDER_SEEDS = List.of(
        // --- Original 30 seeds ---
        new OrderSeed(0,  OrderStatus.PENDING,    0, -1, false, "Grilled Ribeye (Imported)", "French Fries", "Caesar Salad"),
        new OrderSeed(0,  OrderStatus.PREPARING,  1, -1, false, "Salmon Fillet", "Seasonal Vegetables"),
        new OrderSeed(1,  OrderStatus.COMPLETED,  2, -1, false, "Classic English Breakfast", "Oats", "Custard"),
        new OrderSeed(1,  OrderStatus.PAID,       3, -1, false, "Chicken Pizza", "Grilled Chicken Burger", "Classic Cheesecake"),
        new OrderSeed(1,  OrderStatus.PAID,      -1, -1, true,  "Shawarma - Chicken", "Shawarma - Beef"),
        new OrderSeed(2,  OrderStatus.COMPLETED,  4, -1, false, "Short Ribs", "Steamed Basmati Rice", "Seasonal Vegetables"),
        new OrderSeed(2,  OrderStatus.CANCELLED,  5, -1, false, "T-Bone Steak (Imported)", "Sweet Potato Fries"),
        new OrderSeed(3,  OrderStatus.PAID,      -1,  0, false, "Safron Signature Breakfast", "Yellow Pap"),
        new OrderSeed(3,  OrderStatus.COMPLETED,  6, -1, false, "Seafood Platter", "Steamed Basmati Rice"),
        new OrderSeed(4,  OrderStatus.PAID,       7, -1, false, "Spaghetti Bolognese", "Garden Salad", "Ice Cream"),
        new OrderSeed(5,  OrderStatus.PAID,      -1,  1, false, "Nigerian Breakfast", "Custard"),
        new OrderSeed(5,  OrderStatus.COMPLETED,  0, -1, false, "Cordon Blue", "Fried Plantain", "Special Safron Brownie"),
        new OrderSeed(6,  OrderStatus.PAID,       1, -1, false, "Signature Platter", "The Safron Smoky Jollof"),
        new OrderSeed(7,  OrderStatus.CANCELLED,  2, -1, false, "Prawn Thermidor", "Steamed Basmati Rice"),
        new OrderSeed(8,  OrderStatus.PAID,      -1, -1, true,  "Classic Club Sandwich", "French Fries"),
        new OrderSeed(8,  OrderStatus.COMPLETED,  3, -1, false, "Roast Chicken - Full Roast", "Coleslaw", "Fried Rice (Side)"),
        new OrderSeed(9,  OrderStatus.PAID,       4, -1, false, "Lamb Chops (Imported)", "Yam Fries", "Greek Salad"),
        new OrderSeed(10, OrderStatus.COMPLETED, -1,  2, false, "Peppered Snails (Igbin)", "Chicken Pepper Soup"),
        new OrderSeed(11, OrderStatus.PAID,       5, -1, false, "Mixed Platter", "Steamed Basmati Rice"),
        new OrderSeed(12, OrderStatus.CANCELLED,  6, -1, false, "Seafood Pasta", "Garden Salad"),
        new OrderSeed(14, OrderStatus.PAID,      -1,  3, false, "Classic English Breakfast", "Waffles", "Strawberry Pancakes"),
        new OrderSeed(16, OrderStatus.COMPLETED,  7, -1, false, "Grilled Croaker Fillet", "The Safron Smoky Jollof", "Seasonal Vegetables"),
        new OrderSeed(18, OrderStatus.PAID,       0, -1, false, "Suya Pizza - Chicken", "Suya Pizza - Beef", "Extra Cheese (Pizza)"),
        new OrderSeed(20, OrderStatus.PAID,      -1, -1, true,  "The Safron Special Burger", "Sweet Potato Fries", "Ice Cream"),
        new OrderSeed(22, OrderStatus.COMPLETED,  1, -1, false, "Seafood Salad", "Prawns Tempura", "Classic Cheesecake"),
        new OrderSeed(24, OrderStatus.CANCELLED,  2, -1, false, "Alfredo - Opt Prawn", "Avocado Salad"),
        new OrderSeed(26, OrderStatus.PAID,      -1,  0, false, "Full Breakfast Buffet", "Yellow Pap"),
        new OrderSeed(28, OrderStatus.COMPLETED,  3, -1, false, "Short Ribs", "Coleslaw", "Event Cake - Chocolate"),
        new OrderSeed(29, OrderStatus.PAID,       4, -1, false, "Mixed Platter", "Fried Rice (Side)", "Goat Meat Pepper Soup"),
        new OrderSeed(30, OrderStatus.PAID,      -1, -1, true,  "Meat Pie", "Chicken Pie", "Doughnut"),

        // --- 20 seeds (originally added to reach 50) ---
        new OrderSeed(0,  OrderStatus.PAID,       5, -1, false, "Prawn Thermidor", "Seasonal Vegetables", "Ice Cream"),
        new OrderSeed(1,  OrderStatus.COMPLETED,  6, -1, true,  "Chicken Wings", "French Fries"),
        new OrderSeed(2,  OrderStatus.PREPARING, -1,  1, false, "Nigerian Classics Combo", "The Safron Smoky Jollof"),
        new OrderSeed(3,  OrderStatus.PENDING,    7, -1, false, "Grilled Ribeye (Imported)", "Mashed Potatoes"),
        new OrderSeed(4,  OrderStatus.PAID,      -1, -1, true,  "Shawarma - Mixed Chicken & Beef", "Doughnut"),
        new OrderSeed(5,  OrderStatus.COMPLETED,  0, -1, false, "Seafood Okro", "Pounded Yam"),
        new OrderSeed(6,  OrderStatus.CANCELLED,  1, -1, false, "T-Bone Steak (Imported)", "Sweet Potato Fries"),
        new OrderSeed(7,  OrderStatus.PAID,      -1,  2, false, "Safron Signature Native Fried Rice", "Turkey Sauce (Protein)"),
        new OrderSeed(8,  OrderStatus.PREPARING,  2, -1, false, "Salmon Fillet", "Butter Corn"),
        new OrderSeed(9,  OrderStatus.COMPLETED,  3, -1, false, "Mixed Platter", "Greek Salad"),
        new OrderSeed(10, OrderStatus.PAID,       4, -1, true,  "Classic Club Sandwich", "Seasonal Vegetables"),
        new OrderSeed(11, OrderStatus.CANCELLED, -1,  3, false, "Nigerian Breakfast", "Yellow Pap"),
        new OrderSeed(13, OrderStatus.PAID,       5, -1, false, "Short Ribs", "Steamed Basmati Rice", "Coleslaw"),
        new OrderSeed(15, OrderStatus.COMPLETED,  6, -1, false, "Suya Pizza - Beef", "Extra Cheese (Pizza)"),
        new OrderSeed(17, OrderStatus.PAID,       7, -1, true,  "The Safron Special Burger", "Yam Fries"),
        new OrderSeed(19, OrderStatus.PREPARING, -1,  0, false, "Peppered Snails (Igbin)", "Chicken Wings"),
        new OrderSeed(21, OrderStatus.COMPLETED,  0, -1, false, "Cordon Blue", "French Fries", "Event Cake - Red Velvet"),
        new OrderSeed(23, OrderStatus.PAID,       1, -1, false, "Seafood Pasta", "Caesar Salad"),
        new OrderSeed(25, OrderStatus.CANCELLED,  2, -1, false, "Prawns Tempura", "Avocado Salad"),
        new OrderSeed(27, OrderStatus.PAID,      -1, -1, true,  "Chicken Pie", "Meat Pie", "Doughnut"),

        // --- 50 additional seeds to reach 100 orders, spread across more days ---
        new OrderSeed(0,  OrderStatus.PAID,       2, -1, false, "Suya Pizza - Chicken", "Coleslaw"),
        new OrderSeed(1,  OrderStatus.PREPARING,  3, -1, false, "Grilled Croaker Fillet", "Seasonal Vegetables"),
        new OrderSeed(2,  OrderStatus.PAID,      -1, -1, true,  "Classic Club Sandwich", "Doughnut"),
        new OrderSeed(3,  OrderStatus.COMPLETED,  4, -1, false, "Seafood Platter", "Garden Salad", "Ice Cream"),
        new OrderSeed(4,  OrderStatus.PENDING,    5, -1, false, "Lamb Chops (Imported)", "Mashed Potatoes"),
        new OrderSeed(5,  OrderStatus.PAID,      -1,  1, false, "Safron Signature Breakfast", "Custard"),
        new OrderSeed(6,  OrderStatus.COMPLETED,  6, -1, false, "Signature Platter", "Steamed Basmati Rice", "Coleslaw"),
        new OrderSeed(7,  OrderStatus.CANCELLED,  7, -1, false, "Alfredo - Opt Prawn", "Garden Salad"),
        new OrderSeed(8,  OrderStatus.PAID,       0, -1, false, "Chicken Pizza", "Extra Cheese (Pizza)"),
        new OrderSeed(9,  OrderStatus.PAID,      -1, -1, true,  "Shawarma - Beef", "Shawarma - Chicken", "Ice Cream"),
        new OrderSeed(10, OrderStatus.COMPLETED,  1, -1, false, "Roast Chicken - Full Roast", "Fried Rice (Side)"),
        new OrderSeed(11, OrderStatus.PAID,       2, -1, false, "Mixed Platter", "Pounded Yam", "Goat Meat Pepper Soup"),
        new OrderSeed(12, OrderStatus.PREPARING, -1,  2, false, "Nigerian Breakfast", "Yellow Pap"),
        new OrderSeed(13, OrderStatus.COMPLETED,  3, -1, false, "Salmon Fillet", "Seasonal Vegetables", "Classic Cheesecake"),
        new OrderSeed(14, OrderStatus.PAID,       4, -1, false, "T-Bone Steak (Imported)", "Sweet Potato Fries"),
        new OrderSeed(15, OrderStatus.CANCELLED,  5, -1, false, "Seafood Pasta", "Avocado Salad"),
        new OrderSeed(16, OrderStatus.PAID,      -1, -1, true,  "The Safron Special Burger", "French Fries", "Doughnut"),
        new OrderSeed(17, OrderStatus.COMPLETED,  6, -1, false, "Short Ribs", "Steamed Basmati Rice"),
        new OrderSeed(18, OrderStatus.PAID,       7, -1, false, "Grilled Chicken Burger", "Sweet Potato Fries"),
        new OrderSeed(19, OrderStatus.PAID,      -1,  3, false, "Full Breakfast Buffet", "Waffles"),
        new OrderSeed(20, OrderStatus.COMPLETED,  0, -1, false, "Seafood Salad", "Prawns Tempura"),
        new OrderSeed(21, OrderStatus.PREPARING,  1, -1, false, "Spaghetti Bolognese", "Garden Salad"),
        new OrderSeed(22, OrderStatus.PAID,      -1, -1, true,  "Meat Pie", "Chicken Pie"),
        new OrderSeed(23, OrderStatus.CANCELLED,  2, -1, false, "Prawn Thermidor", "Seasonal Vegetables"),
        new OrderSeed(24, OrderStatus.PAID,       3, -1, false, "Cordon Blue", "Fried Plantain", "Special Safron Brownie"),
        new OrderSeed(25, OrderStatus.COMPLETED, -1,  0, false, "Nigerian Classics Combo", "The Safron Smoky Jollof"),
        new OrderSeed(26, OrderStatus.PAID,       4, -1, false, "Suya Pizza - Beef", "Extra Cheese (Pizza)"),
        new OrderSeed(27, OrderStatus.PAID,      -1, -1, true,  "Chicken Wings", "French Fries"),
        new OrderSeed(28, OrderStatus.COMPLETED,  5, -1, false, "Grilled Ribeye (Imported)", "Caesar Salad", "Ice Cream"),
        new OrderSeed(29, OrderStatus.PREPARING,  6, -1, false, "Peppered Snails (Igbin)", "Chicken Pepper Soup"),
        new OrderSeed(31, OrderStatus.PAID,       7, -1, false, "Mixed Platter", "Steamed Basmati Rice"),
        new OrderSeed(32, OrderStatus.COMPLETED, -1,  1, false, "Classic English Breakfast", "Oats", "Custard"),
        new OrderSeed(33, OrderStatus.PAID,       0, -1, false, "Seafood Okro", "Pounded Yam"),
        new OrderSeed(34, OrderStatus.CANCELLED,  1, -1, false, "Alfredo - Opt Prawn", "Avocado Salad"),
        new OrderSeed(35, OrderStatus.PAID,      -1, -1, true,  "Shawarma - Mixed Chicken & Beef", "Doughnut"),
        new OrderSeed(36, OrderStatus.COMPLETED,  2, -1, false, "Lamb Chops (Imported)", "Yam Fries", "Greek Salad"),
        new OrderSeed(37, OrderStatus.PAID,       3, -1, false, "Safron Signature Native Fried Rice", "Turkey Sauce (Protein)"),
        new OrderSeed(38, OrderStatus.PREPARING, -1,  2, false, "Safron Signature Breakfast", "Yellow Pap"),
        new OrderSeed(39, OrderStatus.PAID,       4, -1, false, "Grilled Croaker Fillet", "The Safron Smoky Jollof", "Seasonal Vegetables"),
        new OrderSeed(40, OrderStatus.COMPLETED,  5, -1, false, "Short Ribs", "Coleslaw", "Event Cake - Chocolate"),
        new OrderSeed(41, OrderStatus.PAID,      -1, -1, true,  "Classic Club Sandwich", "French Fries", "Ice Cream"),
        new OrderSeed(42, OrderStatus.CANCELLED,  6, -1, false, "Seafood Pasta", "Garden Salad"),
        new OrderSeed(43, OrderStatus.PAID,       7, -1, false, "Signature Platter", "The Safron Smoky Jollof"),
        new OrderSeed(44, OrderStatus.COMPLETED, -1,  3, false, "Nigerian Breakfast", "Custard", "Waffles"),
        new OrderSeed(45, OrderStatus.PAID,       0, -1, false, "Suya Pizza - Chicken", "Suya Pizza - Beef", "Extra Cheese (Pizza)"),
        new OrderSeed(46, OrderStatus.PREPARING,  1, -1, false, "Salmon Fillet", "Butter Corn"),
        new OrderSeed(47, OrderStatus.PAID,      -1, -1, true,  "The Safron Special Burger", "Sweet Potato Fries"),
        new OrderSeed(48, OrderStatus.COMPLETED,  2, -1, false, "Seafood Platter", "Steamed Basmati Rice"),
        new OrderSeed(49, OrderStatus.PAID,       3, -1, false, "Roast Chicken - Full Roast", "Fried Rice (Side)", "Classic Cheesecake"),
        new OrderSeed(50, OrderStatus.CANCELLED, -1,  0, false, "T-Bone Steak (Imported)", "Sweet Potato Fries")
    );

    @Bean
    @org.springframework.core.annotation.Order(8)
    @Transactional
    CommandLineRunner seedOrders() {
        return args -> {
            if (orderRepository.count() > 0) {
                log.info("ℹ️ Order seeding skipped: Orders already exist.");
                return;
            }

            List<Account> waiters  = accountRepository.findByRole(Role.ROLE_WAITER);
            List<Account> cashiers = accountRepository.findByRole(Role.ROLE_CASHIER);

            List<Customer> customers = customerRepository.findAll();

            List<MenuItem> menuItems = menuItemRepository.findAll();
            List<RestaurantTable> tables = tableRepository.findAll();
            List<Room> rooms = roomRepository.findAll();

            if (menuItems.isEmpty()) {
                log.warn("⚠️ Order seeding skipped: No menu items found.");
                return;
            }

            int saved = 0;
            ZoneId localZone = ZoneId.systemDefault();

            for (int i = 0; i < ORDER_SEEDS.size(); i++) {
                OrderSeed seed = ORDER_SEEDS.get(i);

                Account waiter  = waiters.isEmpty()  ? null : waiters.get(i % waiters.size());
                Account cashier = cashiers.isEmpty() ? null : cashiers.get(i % cashiers.size());

                Customer customer = customers.isEmpty() ? null : customers.get(i % customers.size());

                RestaurantTable table = (seed.tableIndex() >= 0 && seed.tableIndex() < tables.size())
                        ? tables.get(seed.tableIndex()) : null;
                Room room = (seed.roomIndex() >= 0 && seed.roomIndex() < rooms.size())
                        ? rooms.get(seed.roomIndex()) : null;

                if (room != null) {
                    waiter = null;
                }

                ZonedDateTime baseTargetDate = ZonedDateTime.now(localZone)
                        .minusDays(seed.daysAgo())
                        .toLocalDate()
                        .atStartOfDay(localZone);

                int hourOffset = 8 + (i % 15);
                int minuteOffset = (i * 7) % 60;
                int secondOffset = (i * 13) % 60;

                Instant timestamp = baseTargetDate
                        .plusHours(hourOffset)
                        .plusMinutes(minuteOffset)
                        .plusSeconds(secondOffset)
                        .toInstant();

                if (seed.daysAgo() == 0 && timestamp.isAfter(Instant.now())) {
                    timestamp = Instant.now().minusSeconds(i * 30L);
                }

                Order order = new Order();
                order.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                order.setStatus(seed.status());
                order.setWaiter(waiter);
                order.setCashier(cashier);
                order.setCustomer(customer);
                order.setTable(table);
                order.setRoom(room);
                order.setTotal(0.0);
                order.setQuantity(0);
                order.setCreatedAt(timestamp);
                order.setUpdatedAt(timestamp);

                for (String itemName : seed.itemNames()) {
                    MenuItem menuItem = findByName(menuItems, itemName);
                    if (menuItem == null) {
                        log.warn("⚠️ MenuItem not found: '{}' — skipping item.", itemName);
                        continue;
                    }
                    OrderItem oi = new OrderItem();
                    oi.setOrder(order);
                    oi.setMenuItem(menuItem);
                    oi.setQuantity(1);
                    oi.setTakeOut(seed.takeOut());
                    oi.setPrice(menuItem.getPrice());
                    order.getItems().add(oi);
                }

                double total = order.getItems().stream().mapToDouble(OrderItem::getPrice).sum();
                int qty      = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
                order.setTotal(total);
                order.setQuantity(qty);

                orderRepository.save(order);
                saved++;
            }

            log.info("""
                    
                    ================================================================
                    🌱 DATABASE SEEDER: ORDERS GENERATED WITH SCATTERED TIMES
                    Successfully seeded {} orders distributed across operational business hours.
                    ================================================================
                    """, saved);
        };
    }

    private MenuItem findByName(List<MenuItem> items, String name) {
        return items.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private record OrderSeed(
            int daysAgo,
            OrderStatus status,
            int tableIndex,
            int roomIndex,
            boolean takeOut,
            String... itemNames
    ) {}
}