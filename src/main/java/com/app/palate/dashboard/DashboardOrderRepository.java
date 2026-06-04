package com.app.palate.dashboard;

import com.app.palate.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DashboardOrderRepository extends JpaRepository<Order, Long> {

        @Query("""
                            SELECT COALESCE(SUM(o.total), 0)
                            FROM Order o
                            WHERE o.status IN (com.app.palate.order.OrderStatus.PAID, com.app.palate.order.OrderStatus.COMPLETED)
                              AND o.createdAt >= :from
                              AND o.createdAt < :to
                        """)
        double sumRevenueBetween(@Param("from") Instant from, @Param("to") Instant to);

        @Query("""
                            SELECT COUNT(o)
                            FROM Order o
                            WHERE o.createdAt >= :from
                              AND o.createdAt < :to
                        """)
        long countOrdersBetween(@Param("from") Instant from, @Param("to") Instant to);

        // 1. Financial: Avg Order Value (Paid only)
        @Query("SELECT o.table.tableName, AVG(o.total) FROM Order o " +
                        "WHERE o.table IS NOT NULL AND o.status = 'PAID' " +
                        "AND o.createdAt BETWEEN :start AND :end GROUP BY o.table.tableName")
        List<Object[]> getAvgOrderValueByTable(@Param("start") Instant start, @Param("end") Instant end);

        // 2. Financial: Paid Order Volume by Hour (For revenue trends)

        // 3. Operational: Total Orders (All statuses, for kitchen/waiter load)
        @Query("SELECT COUNT(o) FROM Order o " +
                        "WHERE o.createdAt BETWEEN :start AND :end")
        long getTotalOrderVolume(@Param("start") Instant start, @Param("end") Instant end);

        // 4. Operational: Peak Hour (All statuses)

        @Query(value = """
                            SELECT CAST(EXTRACT(HOUR FROM created_at AT TIME ZONE 'UTC') AS int) AS hour,
                                   COUNT(id) AS cnt
                            FROM orders
                            WHERE status = 'PAID'
                              AND created_at BETWEEN :start AND :end
                            GROUP BY EXTRACT(HOUR FROM created_at AT TIME ZONE 'UTC')
                        """, nativeQuery = true)
        List<Object[]> getPaidOrdersVolumeByHour(@Param("start") Instant start, @Param("end") Instant end);

        @Query(value = """
                            SELECT CAST(EXTRACT(HOUR FROM created_at AT TIME ZONE 'UTC') AS int) AS hour,
                                   COUNT(id) AS total
                            FROM orders
                            WHERE created_at BETWEEN :start AND :end
                            GROUP BY EXTRACT(HOUR FROM created_at AT TIME ZONE 'UTC')
                            ORDER BY total DESC
                        """, nativeQuery = true)
        List<Object[]> getPeakOrderHour(@Param("start") Instant start, @Param("end") Instant end);
}