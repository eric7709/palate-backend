package com.app.palate.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

            // 1. Create the list instead of a single predicate variable
            List <Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (waiterId != null) {
                predicates.add(cb.equal(root.get("waiter").get("id"), waiterId));
            }

            if (cashierId != null) {
                predicates.add(cb.equal(root.get("cashier").get("id"), cashierId));
            }

            if (tableId != null) {
                predicates.add(cb.equal(root.get("table").get("id"), tableId));
            }

            if (minTotal != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("total"), minTotal));
            }

            if (maxTotal != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("total"), maxTotal));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), endDate));
            }

            if (search != null && !search.isBlank()) {
                String term = "%" + search.toLowerCase() + "%";

                Predicate invoiceLike = cb.like(
                        cb.lower(root.get("invoiceNumber")), term);

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
                        cb.lower(tableJoin.get("tab leNumber").as(String.class)), term);
                Predicate tableLike = cb.or(tableNameLike, tableNumberLike);

                Subquery<Long> itemSubquery = query.subquery(Long.class);
                Root<Order> subRoot = itemSubquery.from(Order.class);
                Join<Order, OrderItem> subItems = subRoot.join("items", JoinType.LEFT);
                Join<OrderItem, MenuItem> subMenuItem = subItems.join("menuItem", JoinType.LEFT);
                itemSubquery.select(cb.literal(1L))
                        .where(
                                cb.equal(subRoot.get("id"), root.get("id")),
                                cb.like(cb.lower(subMenuItem.get("name")), term));
                Predicate menuItemLike = cb.exists(itemSubquery);

                // 2. Add the entire OR search block as one single predicate into the list
                predicates.add(cb.or(
                        invoiceLike,
                        customerLike,
                        waiterLike,
                        cashierLike,
                        tableLike,
                        menuItemLike));
            }

            // 3. Combine everything at once at the end
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}