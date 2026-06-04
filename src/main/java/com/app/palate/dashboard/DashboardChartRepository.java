package com.app.palate.dashboard;

import com.app.palate.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DashboardChartRepository extends JpaRepository<Order, Long> {

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
    List<DailyRevenueProjection> findDailyRevenue(
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query(value = """
                SELECT EXTRACT(HOUR FROM created_at AT TIME ZONE 'UTC')::int AS hour,
                       SUM(total)                                             AS revenue,
                       COUNT(id)                                              AS orderCount
                FROM orders
                WHERE status IN ('PAID', 'COMPLETED')
                  AND created_at >= :from
                  AND created_at < :to
                GROUP BY EXTRACT(HOUR FROM created_at AT TIME ZONE 'UTC')
                ORDER BY EXTRACT(HOUR FROM created_at AT TIME ZONE 'UTC') ASC
            """, nativeQuery = true)
    List<HourlyRevenueProjection> findHourlyRevenue(
            @Param("from") Instant from,
            @Param("to") Instant to);

    // ── Projections ───────────────────────────────────────────────────────

    interface DailyRevenueProjection {
        java.time.LocalDate getDay();

        Double getRevenue();

        Long getOrderCount();
    }

    interface HourlyRevenueProjection {
        Integer getHour();

        Double getRevenue();

        Long getOrderCount();
    }
}