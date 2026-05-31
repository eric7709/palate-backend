package com.app.palate.analytics;

import com.app.palate.analytics.projections.*;
import com.app.palate.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Order, Long> {

    // ── Revenue ──────────────────────────────────────────────────────────────

    @Query("""
        SELECT
            COALESCE(SUM(o.total), 0) as totalRevenue,
            COALESCE(AVG(o.total), 0) as avgOrderValue,
            COUNT(o)                  as totalOrders
        FROM Order o
        WHERE o.status IN ('COMPLETED', 'PAID')
        AND o.createdAt BETWEEN :startDate AND :endDate
    """)
    RevenueSummaryProjection getRevenueSummary(
            @Param("startDate") Instant startDate,
            @Param("endDate")   Instant endDate
    );

    @Query(value = """
        SELECT
            DATE_TRUNC(:granularity, created_at) AS period,
            SUM(total)                            AS revenue,
            COUNT(*)                              AS orderCount
        FROM orders
        WHERE status IN ('COMPLETED', 'PAID')
        AND created_at BETWEEN :startDate AND :endDate
        GROUP BY period
        ORDER BY period
    """, nativeQuery = true)
    List<RevenueOverTimeProjection> getRevenueOverTime(
            @Param("granularity") String  granularity,
            @Param("startDate")   Instant startDate,
            @Param("endDate")     Instant endDate
    );

    // ── Orders ───────────────────────────────────────────────────────────────

    @Query("""
        SELECT o.status AS status, COUNT(o) AS count
        FROM Order o
        WHERE o.createdAt BETWEEN :startDate AND :endDate
        GROUP BY o.status
    """)
    List<OrdersByStatusProjection> countByStatus(
            @Param("startDate") Instant startDate,
            @Param("endDate")   Instant endDate
    );

    @Query(value = """
        SELECT
            EXTRACT(HOUR FROM created_at) AS hour,
            COUNT(*)                      AS count
        FROM orders
        WHERE created_at BETWEEN :startDate AND :endDate
        GROUP BY hour
        ORDER BY hour
    """, nativeQuery = true)
    List<PeakHourProjection> getPeakHours(
            @Param("startDate") Instant startDate,
            @Param("endDate")   Instant endDate
    );

    // ── Menu ─────────────────────────────────────────────────────────────────

    @Query("""
        SELECT
            oi.menuItem.id              AS itemId,
            oi.menuItem.name            AS itemName,
            SUM(oi.quantity)            AS totalQuantity,
            SUM(oi.price * oi.quantity) AS totalRevenue
        FROM OrderItem oi
        WHERE oi.order.status IN ('COMPLETED', 'PAID')
        AND oi.order.createdAt BETWEEN :startDate AND :endDate
        GROUP BY oi.menuItem.id, oi.menuItem.name
        ORDER BY SUM(oi.quantity) DESC
    """)
    List<MenuItemPerformanceProjection> getTopItems(
            @Param("startDate") Instant  startDate,
            @Param("endDate")   Instant  endDate,
            Pageable pageable
    );

    @Query("""
        SELECT
            oi.menuItem.id   AS itemId,
            oi.menuItem.name AS itemName,
            COUNT(oi)        AS cancelCount
        FROM OrderItem oi
        WHERE oi.order.status = 'CANCELLED'
        AND oi.order.createdAt BETWEEN :startDate AND :endDate
        GROUP BY oi.menuItem.id, oi.menuItem.name
        ORDER BY COUNT(oi) DESC
    """)
    List<CancelledItemProjection> getMostCancelledItems(
            @Param("startDate") Instant  startDate,
            @Param("endDate")   Instant  endDate,
            Pageable pageable
    );

    // ── Customers ────────────────────────────────────────────────────────────

    @Query(value = """
        SELECT
            COUNT(DISTINCT CASE
                WHEN first_order BETWEEN :startDate AND :endDate
                THEN customer_id END)                                      AS newCustomers,
            COUNT(DISTINCT CASE
                WHEN first_order < :startDate
                AND  last_order  BETWEEN :startDate AND :endDate
                THEN customer_id END)                                      AS returningCustomers
        FROM (
            SELECT customer_id,
                   MIN(created_at) AS first_order,
                   MAX(created_at) AS last_order
            FROM orders
            WHERE customer_id IS NOT NULL
            GROUP BY customer_id
        ) sub
    """, nativeQuery = true)
    CustomerSummaryProjection getCustomerSummary(
            @Param("startDate") Instant startDate,
            @Param("endDate")   Instant endDate
    );

    @Query("""
        SELECT
            o.customer.id   AS customerId,
            o.customer.name AS customerName,
            COUNT(o)        AS orderCount,
            SUM(o.total)    AS totalSpent
        FROM Order o
        WHERE o.customer IS NOT NULL
        AND o.createdAt BETWEEN :startDate AND :endDate
        GROUP BY o.customer.id, o.customer.name
        ORDER BY COUNT(o) DESC
    """)
    List<TopCustomerProjection> getTopCustomers(
            @Param("startDate") Instant  startDate,
            @Param("endDate")   Instant  endDate,
            Pageable pageable
    );

    // ── Staff ────────────────────────────────────────────────────────────────

    @Query("""
        SELECT
            o.waiter.id                                          AS staffId,
            CONCAT(o.waiter.firstName, ' ', o.waiter.lastName)  AS staffName,
            COUNT(o)                                             AS orderCount,
            SUM(o.total)                                         AS totalValue
        FROM Order o
        WHERE o.waiter IS NOT NULL
        AND o.createdAt BETWEEN :startDate AND :endDate
        GROUP BY o.waiter.id, o.waiter.firstName, o.waiter.lastName
        ORDER BY COUNT(o) DESC
    """)
    List<StaffPerformanceProjection> getOrdersPerWaiter(
            @Param("startDate") Instant startDate,
            @Param("endDate")   Instant endDate
    );

    @Query("""
        SELECT
            o.cashier.id                                           AS staffId,
            CONCAT(o.cashier.firstName, ' ', o.cashier.lastName)  AS staffName,
            COUNT(o)                                               AS orderCount,
            SUM(o.total)                                           AS totalValue
        FROM Order o
        WHERE o.cashier IS NOT NULL
        AND o.status = 'PAID'
        AND o.createdAt BETWEEN :startDate AND :endDate
        GROUP BY o.cashier.id, o.cashier.firstName, o.cashier.lastName
        ORDER BY SUM(o.total) DESC
    """)
    List<StaffPerformanceProjection> getRevenuePerCashier(
            @Param("startDate") Instant startDate,
            @Param("endDate")   Instant endDate
    );

    // ── Tables ───────────────────────────────────────────────────────────────

    @Query("""
        SELECT
            o.table.id          AS tableId,
            o.table.tableName   AS tableName,
            o.table.tableNumber AS tableNumber,
            COUNT(o)            AS orderCount,
            SUM(o.total)        AS totalRevenue
        FROM Order o
        WHERE o.table IS NOT NULL
        AND o.createdAt BETWEEN :startDate AND :endDate
        GROUP BY o.table.id, o.table.tableName, o.table.tableNumber
        ORDER BY COUNT(o) DESC
    """)
    List<TableActivityProjection> getTableActivity(
            @Param("startDate") Instant startDate,
            @Param("endDate")   Instant endDate
    );
}