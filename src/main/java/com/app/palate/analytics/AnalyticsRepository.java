package com.app.palate.analytics;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.app.palate.order.Order;

import java.time.Instant;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Order, Long> {

       // ─── TOP BY SALES ──────────────────────────────────────────────────────────

       @Query("""
                         SELECT o.waiter.id,
                                CONCAT(o.waiter.firstName, ' ', o.waiter.lastName),
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.waiter IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.waiter.id, o.waiter.firstName, o.waiter.lastName
                         ORDER BY SUM(o.total) DESC
                     """)
       List<Object[]> topWaitersBySales(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT o.cashier.id,
                                CONCAT(o.cashier.firstName, ' ', o.cashier.lastName),
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.cashier IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.cashier.id, o.cashier.firstName, o.cashier.lastName
                         ORDER BY SUM(o.total) DESC
                     """)
       List<Object[]> topCashiersBySales(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT o.table.id,
                                o.table.tableName,
                                o.table.tableNumber,
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.table IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.table.id, o.table.tableName, o.table.tableNumber
                         ORDER BY SUM(o.total) DESC
                     """)
       List<Object[]> topTablesBySales(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT mi.category.id,
                                mi.category.name,
                                SUM(oi.price * oi.quantity),
                                SUM(oi.quantity)
                         FROM OrderItem oi
                         JOIN oi.order o
                         JOIN oi.menuItem mi
                         WHERE mi.category IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY mi.category.id, mi.category.name
                         ORDER BY SUM(oi.price * oi.quantity) DESC
                     """)
       List<Object[]> topCategoriesBySales(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT oi.menuItem.id,
                                oi.menuItem.name,
                                mi.category.name,
                                SUM(oi.price * oi.quantity),
                                SUM(oi.quantity)
                         FROM OrderItem oi
                         JOIN oi.order o
                         JOIN oi.menuItem mi
                         WHERE o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY oi.menuItem.id, oi.menuItem.name, mi.category.name
                         ORDER BY SUM(oi.price * oi.quantity) DESC
                     """)
       List<Object[]> topMenuItemsBySales(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT o.customer.id,
                                o.customer.name,
                                o.customer.phoneNumber,
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.customer IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.customer.id, o.customer.name, o.customer.phoneNumber
                         ORDER BY SUM(o.total) DESC
                     """)
       List<Object[]> topCustomersBySales(@Param("from") Instant from, @Param("to") Instant to);

       // ─── TOP BY COUNT ──────────────────────────────────────────────────────────

       @Query("""
                         SELECT o.waiter.id,
                                CONCAT(o.waiter.firstName, ' ', o.waiter.lastName),
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.waiter IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.waiter.id, o.waiter.firstName, o.waiter.lastName
                         ORDER BY COUNT(o) DESC
                     """)
       List<Object[]> topWaitersByCount(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT o.cashier.id,
                                CONCAT(o.cashier.firstName, ' ', o.cashier.lastName),
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.cashier IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.cashier.id, o.cashier.firstName, o.cashier.lastName
                         ORDER BY COUNT(o) DESC
                     """)
       List<Object[]> topCashiersByCount(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT o.table.id,
                                o.table.tableName,
                                o.table.tableNumber,
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.table IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.table.id, o.table.tableName, o.table.tableNumber
                         ORDER BY COUNT(o) DESC
                     """)
       List<Object[]> topTablesByCount(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT mi.category.id,
                                mi.category.name,
                                SUM(oi.price * oi.quantity),
                                SUM(oi.quantity)
                         FROM OrderItem oi
                         JOIN oi.order o
                         JOIN oi.menuItem mi
                         WHERE mi.category IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY mi.category.id, mi.category.name
                         ORDER BY SUM(oi.quantity) DESC
                     """)
       List<Object[]> topCategoriesByCount(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT oi.menuItem.id,
                                oi.menuItem.name,
                                mi.category.name,
                                SUM(oi.price * oi.quantity),
                                SUM(oi.quantity)
                         FROM OrderItem oi
                         JOIN oi.order o
                         JOIN oi.menuItem mi
                         WHERE o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY oi.menuItem.id, oi.menuItem.name, mi.category.name
                         ORDER BY SUM(oi.quantity) DESC
                     """)
       List<Object[]> topMenuItemsByCount(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT o.customer.id,
                                o.customer.name,
                                o.customer.phoneNumber,
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.customer IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.customer.id, o.customer.name, o.customer.phoneNumber
                         ORDER BY COUNT(o) DESC
                     """)
       List<Object[]> topCustomersByCount(@Param("from") Instant from, @Param("to") Instant to);

       // topCustomersByFrequency removed — was identical to topCustomersByCount

       // ─── LEAST BY SALES ────────────────────────────────────────────────────────

       @Query("""
                         SELECT o.waiter.id,
                                CONCAT(o.waiter.firstName, ' ', o.waiter.lastName),
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.waiter IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.waiter.id, o.waiter.firstName, o.waiter.lastName
                         ORDER BY SUM(o.total) ASC
                     """)
       List<Object[]> leastWaitersBySales(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT o.cashier.id,
                                CONCAT(o.cashier.firstName, ' ', o.cashier.lastName),
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.cashier IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.cashier.id, o.cashier.firstName, o.cashier.lastName
                         ORDER BY SUM(o.total) ASC
                     """)
       List<Object[]> leastCashiersBySales(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT o.table.id,
                                o.table.tableName,
                                o.table.tableNumber,
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.table IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.table.id, o.table.tableName, o.table.tableNumber
                         ORDER BY SUM(o.total) ASC
                     """)
       List<Object[]> leastTablesBySales(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT mi.category.id,
                                mi.category.name,
                                SUM(oi.price * oi.quantity),
                                SUM(oi.quantity)
                         FROM OrderItem oi
                         JOIN oi.order o
                         JOIN oi.menuItem mi
                         WHERE mi.category IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY mi.category.id, mi.category.name
                         ORDER BY SUM(oi.price * oi.quantity) ASC
                     """)
       List<Object[]> leastCategoriesBySales(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT oi.menuItem.id,
                                oi.menuItem.name,
                                mi.category.name,
                                SUM(oi.price * oi.quantity),
                                SUM(oi.quantity)
                         FROM OrderItem oi
                         JOIN oi.order o
                         JOIN oi.menuItem mi
                         WHERE o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY oi.menuItem.id, oi.menuItem.name, mi.category.name
                         ORDER BY SUM(oi.price * oi.quantity) ASC
                     """)
       List<Object[]> leastMenuItemsBySales(@Param("from") Instant from, @Param("to") Instant to);

       // ─── LEAST BY COUNT ────────────────────────────────────────────────────────

       @Query("""
                         SELECT o.waiter.id,
                                CONCAT(o.waiter.firstName, ' ', o.waiter.lastName),
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.waiter IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.waiter.id, o.waiter.firstName, o.waiter.lastName
                         ORDER BY COUNT(o) ASC
                     """)
       List<Object[]> leastWaitersByCount(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT o.cashier.id,
                                CONCAT(o.cashier.firstName, ' ', o.cashier.lastName),
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.cashier IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.cashier.id, o.cashier.firstName, o.cashier.lastName
                         ORDER BY COUNT(o) ASC
                     """)
       List<Object[]> leastCashiersByCount(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT o.table.id,
                                o.table.tableName,
                                o.table.tableNumber,
                                SUM(o.total),
                                COUNT(o)
                         FROM Order o
                         WHERE o.table IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY o.table.id, o.table.tableName, o.table.tableNumber
                         ORDER BY COUNT(o) ASC
                     """)
       List<Object[]> leastTablesByCount(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT mi.category.id,
                                mi.category.name,
                                SUM(oi.price * oi.quantity),
                                SUM(oi.quantity)
                         FROM OrderItem oi
                         JOIN oi.order o
                         JOIN oi.menuItem mi
                         WHERE mi.category IS NOT NULL AND o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY mi.category.id, mi.category.name
                         ORDER BY SUM(oi.quantity) ASC
                     """)
       List<Object[]> leastCategoriesByCount(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT oi.menuItem.id,
                                oi.menuItem.name,
                                mi.category.name,
                                SUM(oi.price * oi.quantity),
                                SUM(oi.quantity)
                         FROM OrderItem oi
                         JOIN oi.order o
                         JOIN oi.menuItem mi
                         WHERE o.status = 'PAID'
                           AND o.createdAt BETWEEN :from AND :to
                         GROUP BY oi.menuItem.id, oi.menuItem.name, mi.category.name
                         ORDER BY SUM(oi.quantity) ASC
                     """)
       List<Object[]> leastMenuItemsByCount(@Param("from") Instant from, @Param("to") Instant to);

       // ─── SUMMARY STATS ─────────────────────────────────────────────────────────

       @Query("SELECT AVG(o.total) FROM Order o WHERE o.status = 'PAID' AND o.createdAt BETWEEN :from AND :to")
       Double averageOrderValue(@Param("from") Instant from, @Param("to") Instant to);

       @Query("SELECT SUM(o.total) FROM Order o WHERE o.status = 'PAID' AND o.createdAt BETWEEN :from AND :to")
       Double totalRevenue(@Param("from") Instant from, @Param("to") Instant to);

       @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PAID' AND o.createdAt BETWEEN :from AND :to")
       Long totalOrderCount(@Param("from") Instant from, @Param("to") Instant to);

       @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'CANCELLED' AND o.createdAt BETWEEN :from AND :to")
       Long cancelledOrderCount(@Param("from") Instant from, @Param("to") Instant to);

       @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN ('PAID','CANCELLED','PREPARING','COMPLETED','PENDING') AND o.createdAt BETWEEN :from AND :to")
       Long totalOrdersPlaced(@Param("from") Instant from, @Param("to") Instant to);

       @Query("SELECT AVG(o.quantity) FROM Order o WHERE o.status = 'PAID' AND o.createdAt BETWEEN :from AND :to")
       Double averageItemsPerOrder(@Param("from") Instant from, @Param("to") Instant to);

       @Query("""
                         SELECT oi.takeOut, COUNT(oi), SUM(oi.price * oi.quantity)
                         FROM OrderItem oi
                         JOIN oi.order o
                         WHERE o.status = 'PAID' AND o.createdAt BETWEEN :from AND :to
                         GROUP BY oi.takeOut
                     """)
       List<Object[]> takeOutVsDineIn(@Param("from") Instant from, @Param("to") Instant to);

       @Query(value = """
                         SELECT
                             SUM(CASE WHEN order_rank = 1 THEN 1 ELSE 0 END),
                             SUM(CASE WHEN order_rank > 1  THEN 1 ELSE 0 END)
                         FROM (
                             SELECT customer_id,
                                    RANK() OVER (PARTITION BY customer_id ORDER BY created_at) AS order_rank
                             FROM orders
                             WHERE status = 'PAID'
                               AND customer_id IS NOT NULL
                               AND created_at BETWEEN :from AND :to
                         ) ranked
                     """, nativeQuery = true)
       List<Object[]> newVsReturningCustomers(@Param("from") Instant from, @Param("to") Instant to);

       // Fixed: replaced MySQL DAYNAME/DAYOFWEEK with PostgreSQL equivalents
       @Query(value = """
                         SELECT TRIM(TO_CHAR(created_at, 'Day'))  AS day_name,
                                COUNT(*)                          AS order_count,
                                SUM(total)                        AS total_sales
                         FROM orders
                         WHERE status = 'PAID'
                           AND created_at BETWEEN :from AND :to
                         GROUP BY TRIM(TO_CHAR(created_at, 'Day')), EXTRACT(DOW FROM created_at)
                         ORDER BY SUM(total) DESC
                     """, nativeQuery = true)
       List<Object[]> salesByDayOfWeek(@Param("from") Instant from, @Param("to") Instant to);

       @Query(value = """
                         SELECT CAST(EXTRACT(HOUR FROM created_at) AS INT) AS hour,
                                COUNT(*)                                    AS order_count,
                                SUM(total)                                  AS total_sales
                         FROM orders
                         WHERE status = 'PAID'
                           AND created_at BETWEEN :from AND :to
                         GROUP BY EXTRACT(HOUR FROM created_at)
                         ORDER BY COUNT(*) DESC
                     """, nativeQuery = true)
       List<Object[]> salesByHour(@Param("from") Instant from, @Param("to") Instant to);

       // Fixed: replaced CAST(... AS DATE) with PostgreSQL ::date cast
       @Query(value = """
                         SELECT CAST(created_at AS date) AS sale_date,
                                COUNT(*)                 AS order_count,
                                SUM(total)               AS total_sales
                         FROM orders
                         WHERE status = 'PAID'
                           AND created_at BETWEEN :from AND :to
                         GROUP BY CAST(created_at AS date)
                         ORDER BY CAST(created_at AS date)
                     """, nativeQuery = true)
       List<Object[]> revenueOverTime(@Param("from") Instant from, @Param("to") Instant to);
}