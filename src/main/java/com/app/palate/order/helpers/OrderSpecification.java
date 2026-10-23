package com.app.palate.order.helpers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.app.palate.auth.Account;
import com.app.palate.menuItem.MenuItem;
import com.app.palate.order.Order;
import com.app.palate.order.enums.OrderStatus;
import com.app.palate.orderItem.OrderItem;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.room.Room;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public class OrderSpecification {

    public static Specification<Order> filter(
            OrderStatus status,
            Long waiterId,
            Long cashierId,
            Long tableId,
            Long roomId,
            Double minTotal,
            Double maxTotal,
            Instant startDate,
            Instant endDate,
            String search) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // =========================
            // Status
            // =========================

            if (status != null) {
                predicates.add(
                        cb.equal(root.get("status"), status));
            }

            // =========================
            // Waiter
            // =========================

            if (waiterId != null) {
                predicates.add(
                        cb.equal(
                                root.get("waiter").get("id"),
                                waiterId));
            }

            // =========================
            // Cashier
            // =========================

            if (cashierId != null) {
                predicates.add(
                        cb.equal(
                                root.get("cashier").get("id"),
                                cashierId));
            }

            // =========================
            // Table
            // =========================

            if (tableId != null) {
                predicates.add(
                        cb.equal(
                                root.get("table").get("id"),
                                tableId));
            }

            // =========================
            // Room
            // =========================

            if (roomId != null) {
                predicates.add(
                        cb.equal(
                                root.get("room").get("id"),
                                roomId));
            }

            // =========================
            // Total Range
            // =========================

            if (minTotal != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("total"),
                                minTotal));
            }

            if (maxTotal != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("total"),
                                maxTotal));
            }

            // =========================
            // Date Range
            // =========================

            if (startDate != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("createdAt"),
                                startDate));
            }

            if (endDate != null) {
                predicates.add(
                        cb.lessThan(
                                root.get("createdAt"),
                                endDate));
            }

            // =========================
            // Search
            // =========================

            if (search != null && !search.isBlank()) {

                String term =
                        "%" + search.toLowerCase().trim() + "%";

                Predicate invoiceLike =
                        cb.like(
                                cb.lower(
                                        root.get("invoiceNumber")),
                                term);
                Predicate noteLike = cb.like(cb.lower(root.get("note")), term);
                Predicate orderStatusLike = cb.like(
                        cb.lower(root.get("status").as(String.class)), term);
                Predicate idLike = cb.like(root.get("id").as(String.class), term);
                Predicate totalLike = cb.like(root.get("total").as(String.class), term);
                Predicate quantityLike = cb.like(root.get("quantity").as(String.class), term);
                Predicate paidAmountLike = cb.like(root.get("paidAmount").as(String.class), term);
                Predicate underpaidLike = cb.like(root.get("isUnderpaid").as(String.class), term);
                Predicate remainingBalanceLike = cb.like(root.get("remainingBalance").as(String.class), term);
                Predicate bankNameLike = cb.like(cb.lower(root.get("virtualBankName")), term);
                Predicate monnifyReferenceLike = cb.like(cb.lower(root.get("monnifyReference")), term);
                Predicate createdAtLike = cb.like(root.get("createdAt").as(String.class), term);
                Predicate updatedAtLike = cb.like(root.get("updatedAt").as(String.class), term);

                Join<Order, Object> customerJoin =
                        root.join("customer", JoinType.LEFT);

                Predicate customerLike =
                        cb.like(
                                cb.lower(
                                        customerJoin.get("name")),
                                term);
                Predicate customerEmailLike = cb.like(cb.lower(customerJoin.get("email")), term);
                Predicate customerPhoneLike = cb.like(customerJoin.get("phoneNumber"), term);

                Join<Order, Account> waiterJoin =
                        root.join("waiter", JoinType.LEFT);

                Predicate waiterLike =
                        cb.like(
                                cb.lower(
                                        cb.concat(
                                                cb.concat(
                                                        waiterJoin.get("firstName"),
                                                        " "),
                                                waiterJoin.get("lastName"))),
                                term);
                Predicate waiterEmailLike = cb.like(cb.lower(waiterJoin.get("email")), term);
                Predicate waiterPhoneLike = cb.like(waiterJoin.get("phoneNumber"), term);

                Join<Order, Account> cashierJoin =
                        root.join("cashier", JoinType.LEFT);

                Predicate cashierLike =
                        cb.like(
                                cb.lower(
                                        cb.concat(
                                                cb.concat(
                                                        cashierJoin.get("firstName"),
                                                        " "),
                                                cashierJoin.get("lastName"))),
                                term);
                Predicate cashierEmailLike = cb.like(cb.lower(cashierJoin.get("email")), term);
                Predicate cashierPhoneLike = cb.like(cashierJoin.get("phoneNumber"), term);

                Join<Order, RestaurantTable> tableJoin =
                        root.join("table", JoinType.LEFT);

                Predicate tableNameLike =
                        cb.like(
                                cb.lower(
                                        tableJoin.get("tableName")),
                                term);

                Predicate tableNumberLike =
                        cb.like(
                                cb.lower(
                                        tableJoin.get("tableNumber")
                                                .as(String.class)),
                                term);
                Predicate tableCapacityLike = cb.like(tableJoin.get("capacity").as(String.class), term);
                Predicate tableQrCodeLike = cb.like(cb.lower(tableJoin.get("qrCode")), term);
                Predicate tableStatusLike = cb.like(
                        cb.lower(tableJoin.get("status").as(String.class)), term);

                Predicate tableLike =
                        cb.or(
                                tableNameLike,
                                tableNumberLike,
                                tableCapacityLike,
                                tableQrCodeLike,
                                tableStatusLike);

                Join<Order, Room> roomJoin =
                        root.join("room", JoinType.LEFT);

                Predicate roomNumberLike =
                        cb.like(
                                cb.lower(
                                        roomJoin.get("roomNumber")),
                                term);

                Predicate floorLike =
                        cb.like(
                                cb.lower(
                                        roomJoin.get("floor")
                                                .as(String.class)),
                                term);
                Predicate roomQrCodeLike = cb.like(cb.lower(roomJoin.get("qrCode")), term);
                Predicate roomStatusLike = cb.like(
                        cb.lower(roomJoin.get("status").as(String.class)), term);

                Predicate roomLike =
                        cb.or(
                                roomNumberLike,
                                floorLike,
                                roomQrCodeLike,
                                roomStatusLike);

                Subquery<Long> itemSubquery =
                        query.subquery(Long.class);

                Root<Order> subRoot =
                        itemSubquery.from(Order.class);

                Join<Order, OrderItem> subItems =
                        subRoot.join("items", JoinType.LEFT);

                Join<OrderItem, MenuItem> subMenuItem =
                        subItems.join("menuItem", JoinType.LEFT);
                Join<MenuItem, Object> subCategory = subMenuItem.join("category", JoinType.LEFT);

                itemSubquery
                        .select(cb.literal(1L))
                        .where(
                                cb.equal(
                                        subRoot.get("id"),
                                        root.get("id")),
                                cb.or(
                                        cb.like(cb.lower(subMenuItem.get("name")), term),
                                        cb.like(cb.lower(subMenuItem.get("description")), term),
                                        cb.like(subMenuItem.get("price").as(String.class), term),
                                        cb.like(cb.lower(subMenuItem.get("status").as(String.class)), term),
                                        cb.like(cb.lower(subMenuItem.get("imageUrl")), term),
                                        cb.like(cb.lower(subCategory.get("name")), term)));

                Predicate menuItemLike =
                        cb.exists(itemSubquery);

                predicates.add(
                        cb.or(
                                invoiceLike,
                                noteLike,
                                orderStatusLike,
                                idLike,
                                totalLike,
                                quantityLike,
                                paidAmountLike,
                                underpaidLike,
                                remainingBalanceLike,
                                bankNameLike,
                                monnifyReferenceLike,
                                createdAtLike,
                                updatedAtLike,
                                customerLike,
                                customerEmailLike,
                                customerPhoneLike,
                                waiterLike,
                                waiterEmailLike,
                                waiterPhoneLike,
                                cashierLike,
                                cashierEmailLike,
                                cashierPhoneLike,
                                tableLike,
                                roomLike,
                                menuItemLike));
            }

            return cb.and(
                    predicates.toArray(new Predicate[0]));
        };
    }
}