package com.app.palate.dashboard;

import com.app.palate.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Order, Long> {

    // ── Revenue & Orders ──────────────────────────────────────────────────────

    // Fixed: primitive double → Double to safely handle null SUM when no rows match
    @Query("""
                SELECT COALESCE(SUM(o.total), 0)
                FROM Order o
                WHERE o.status IN (com.app.palate.order.OrderStatus.PAID, com.app.palate.order.OrderStatus.COMPLETED)
                  AND o.createdAt >= :from
                  AND o.createdAt < :to
            """)
    Double sumRevenueBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
                SELECT COUNT(o)
                FROM Order o
                WHERE o.createdAt >= :from
                  AND o.createdAt < :to
            """)
    Long countOrdersBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
                SELECT COUNT(o)
                FROM Order o
                WHERE o.createdAt BETWEEN :start AND :end
            """)
    Long getTotalOrderVolume(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
                SELECT o.table.tableName, AVG(o.total)
                FROM Order o
                WHERE o.table IS NOT NULL AND o.status = 'PAID'
                  AND o.createdAt BETWEEN :start AND :end
                GROUP BY o.table.tableName
            """)
    List<Object[]> getAvgOrderValueByTable(@Param("start") Instant start, @Param("end") Instant end);

    @Query(value = """
                SELECT CAST(EXTRACT(HOUR FROM created_at) AS INT) AS hour,
                       COUNT(id)                                   AS cnt
                FROM orders
                WHERE status = 'PAID'
                  AND created_at BETWEEN :start AND :end
                GROUP BY EXTRACT(HOUR FROM created_at)
            """, nativeQuery = true)
    List<Object[]> getPaidOrdersVolumeByHour(@Param("start") Instant start, @Param("end") Instant end);

    @Query(value = """
                SELECT CAST(EXTRACT(HOUR FROM created_at) AS INT) AS hour,
                       COUNT(id)                                   AS total
                FROM orders
                WHERE created_at BETWEEN :start AND :end
                GROUP BY EXTRACT(HOUR FROM created_at)
                ORDER BY total DESC
            """, nativeQuery = true)
    List<Object[]> getPeakOrderHour(@Param("start") Instant start, @Param("end") Instant end);

    // ── Charts ────────────────────────────────────────────────────────────────

    @Query("""
                SELECT CAST(o.createdAt AS date) AS day,
                       SUM(o.total)              AS revenue,
                       COUNT(o)                  AS orderCount
                FROM Order o
                WHERE o.status IN (com.app.palate.order.OrderStatus.PAID, com.app.palate.order.OrderStatus.COMPLETED)
                  AND o.createdAt >= :from
                  AND o.createdAt < :to
                GROUP BY CAST(o.createdAt AS date)
                ORDER BY CAST(o.createdAt AS date) ASC
            """)
    List<DailyRevenueProjection> findDailyRevenue(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
                SELECT CAST(EXTRACT(HOUR FROM created_at) AS INT) AS hour,
                       SUM(total)                                  AS revenue,
                       COUNT(id)                                   AS orderCount
                FROM orders
                WHERE status IN ('PAID', 'COMPLETED')
                  AND created_at >= :from
                  AND created_at < :to
                GROUP BY EXTRACT(HOUR FROM created_at)
                ORDER BY EXTRACT(HOUR FROM created_at) ASC
            """, nativeQuery = true)
    List<HourlyRevenueProjection> findHourlyRevenue(@Param("from") Instant from, @Param("to") Instant to);

    // ── Top Tables ────────────────────────────────────────────────────────────

    // Fixed: LIMIT in JPQL is unsupported — use Pageable instead
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
            """)
    List<Object[]> findTopTables(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    // ── Top Categories ────────────────────────────────────────────────────────

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
            """)
    List<Object[]> findTopCategories(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    // ── Top Items ─────────────────────────────────────────────────────────────

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
            """)
    List<Object[]> findTopItems(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    // ── Top Waiters ───────────────────────────────────────────────────────────

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
            """)
    List<Object[]> findTopWaiters(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    // ── Projections ───────────────────────────────────────────────────────────

    interface DailyRevenueProjection {
        LocalDate getDay();
        Double getRevenue();
        Long getOrderCount();
    }

    interface HourlyRevenueProjection {
        Integer getHour();
        Double getRevenue();
        Long getOrderCount();
    }
}