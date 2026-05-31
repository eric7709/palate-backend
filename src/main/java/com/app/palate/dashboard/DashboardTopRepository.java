package com.app.palate.dashboard;

import com.app.palate.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DashboardTopRepository extends JpaRepository<Order, Long> {

    @Query("""
                SELECT o.table.id,
                       o.table.tableName,
                       o.table.tableNumber,
                       SUM(o.total),
                       COUNT(o)
                FROM Order o
                WHERE o.table IS NOT NULL
                  AND o.status IN (com.app.palate.order.OrderStatus.PAID, com.app.palate.order.OrderStatus.COMPLETED)
                  AND o.createdAt >= :from
                  AND o.createdAt < :to
                GROUP BY o.table.id, o.table.tableName, o.table.tableNumber
                ORDER BY SUM(o.total) DESC
                LIMIT :limit
            """)
    List<Object[]> findTopTables(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("limit") int limit);

    @Query("""
                SELECT oi.menuItem.category.id,
                       oi.menuItem.category.name,
                       SUM(oi.price * oi.quantity),
                       SUM(oi.quantity)
                FROM OrderItem oi
                WHERE oi.menuItem IS NOT NULL
                  AND oi.menuItem.category IS NOT NULL
                  AND oi.order.status IN (com.app.palate.order.OrderStatus.PAID, com.app.palate.order.OrderStatus.COMPLETED)
                  AND oi.order.createdAt >= :from
                  AND oi.order.createdAt < :to
                GROUP BY oi.menuItem.category.id, oi.menuItem.category.name
                ORDER BY SUM(oi.price * oi.quantity) DESC
                LIMIT :limit
            """)
    List<Object[]> findTopCategories(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("limit") int limit);

    @Query("""
                SELECT oi.menuItem.id,
                       oi.menuItem.name,
                       oi.menuItem.category.name,
                       SUM(oi.price * oi.quantity),
                       SUM(oi.quantity)
                FROM OrderItem oi
                WHERE oi.menuItem IS NOT NULL
                  AND oi.order.status IN (com.app.palate.order.OrderStatus.PAID, com.app.palate.order.OrderStatus.COMPLETED)
                  AND oi.order.createdAt >= :from
                  AND oi.order.createdAt < :to
                GROUP BY oi.menuItem.id, oi.menuItem.name, oi.menuItem.category.name
                ORDER BY SUM(oi.price * oi.quantity) DESC
                LIMIT :limit
            """)
    List<Object[]> findTopItems(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("limit") int limit);

    @Query("""
                SELECT o.waiter.id,
                       CONCAT(o.waiter.firstName, ' ', o.waiter.lastName),
                       SUM(o.total),
                       COUNT(o)
                FROM Order o
                WHERE o.waiter IS NOT NULL
                  AND o.status IN (com.app.palate.order.OrderStatus.PAID, com.app.palate.order.OrderStatus.COMPLETED)
                  AND o.createdAt >= :from
                  AND o.createdAt < :to
                GROUP BY o.waiter.id, o.waiter.firstName, o.waiter.lastName
                ORDER BY SUM(o.total) DESC
                LIMIT :limit
            """)
    List<Object[]> findTopWaiters(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("limit") int limit);
}