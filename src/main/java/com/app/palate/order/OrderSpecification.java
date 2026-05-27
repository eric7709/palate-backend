package com.app.palate.order;

import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

import com.app.palate.auth.Account;
import com.app.palate.menuItem.MenuItem;
import com.app.palate.orderItem.OrderItem;
import com.app.palate.restaurantTable.RestaurantTable;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Root;

public class OrderSpecification {

    public static Specification<Order> filter(
            OrderStatus status,
            Long waiterId,
            Long cashierId,
            Long tableId,
            Double minTotal,
            Double maxTotal,
            Instant startDate,
            Instant endDate,
            String search) {

        return (root, query, cb) -> {

            Predicate predicate = cb.conjunction();

            if (status != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("orderStatus"), status));
            }

            if (waiterId != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("waiter").get("id"), waiterId));
            }

            if (cashierId != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("cashier").get("id"), cashierId));
            }

            if (tableId != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("table").get("id"), tableId));
            }

            if (minTotal != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("total"), minTotal));
            }

            if (maxTotal != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("total"), maxTotal));
            }

            // FIX 1: Use half-open interval [startDate, endDate) for date range.
            // endDate is already set to next-day midnight in the service layer,
            // so we use lessThan (exclusive) instead of lessThanOrEqualTo.
            if (startDate != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            if (endDate != null) {
                predicate = cb.and(predicate,
                        cb.lessThan(root.get("createdAt"), endDate));
            }

            // FIX 2: Search uses subqueries to avoid cross-join duplicates caused
            // by multiple LEFT JOINs on collection associations (items → menuItem).
            // This eliminates the need for query.distinct(true) which kills pagination.
            if (search != null && !search.isBlank()) {
                String term = "%" + search.toLowerCase() + "%";

                Predicate invoiceLike = cb.like(
                        cb.lower(root.get("invoiceNumber")), term);

                // FIX 3: customer join changed from INNER to LEFT so orders
                // without a customer are not silently excluded.
                Join<Order, Object> customerJoin = root.join("customer", JoinType.LEFT);
                Predicate customerLike = cb.like(
                        cb.lower(customerJoin.get("name")), term);

                Join<Order, Account> waiterJoin = root.join("waiter", JoinType.LEFT);
                Predicate waiterLike = cb.like(
                        cb.lower(cb.concat(
                                cb.concat(waiterJoin.get("firstName"), " "),
                                waiterJoin.get("lastName"))),
                        term);

                Join<Order, Account> cashierJoin = root.join("cashier", JoinType.LEFT);
                Predicate cashierLike = cb.like(
                        cb.lower(cb.concat(
                                cb.concat(cashierJoin.get("firstName"), " "),
                                cashierJoin.get("lastName"))),
                        term);

                Join<Order, RestaurantTable> tableJoin = root.join("table", JoinType.LEFT);
                Predicate tableNameLike = cb.like(
                        cb.lower(tableJoin.get("tableName")), term);
                Predicate tableNumberLike = cb.like(
                        cb.lower(tableJoin.get("tableNumber").as(String.class)), term);
                Predicate tableLike = cb.or(tableNameLike, tableNumberLike);

                // FIX 4: Use EXISTS subquery for the collection join (items → menuItem)
                // instead of a direct LEFT JOIN. A direct join on a @OneToMany produces
                // one row per item, inflating the result set and breaking pagination
                // even with DISTINCT (SQL DISTINCT + ORDER BY requires all sorted columns
                // in the SELECT, which Spring Data can't guarantee on a Page query).
                Subquery<Long> itemSubquery = query.subquery(Long.class);
                Root<Order> subRoot = itemSubquery.from(Order.class);
                Join<Order, OrderItem> subItems = subRoot.join("items", JoinType.LEFT);
                Join<OrderItem, MenuItem> subMenuItem = subItems.join("menuItem", JoinType.LEFT);
                itemSubquery.select(cb.literal(1L))
                        .where(
                                cb.equal(subRoot.get("id"), root.get("id")),
                                cb.like(cb.lower(subMenuItem.get("name")), term));
                Predicate menuItemLike = cb.exists(itemSubquery);

                predicate = cb.and(predicate, cb.or(
                        invoiceLike,
                        customerLike,
                        waiterLike,
                        cashierLike,
                        tableLike,
                        menuItemLike));

                // No longer needed — subquery eliminates duplicates at the source.
                // query.distinct(true) removed intentionally.
            }

            return predicate;
        };
    }
}