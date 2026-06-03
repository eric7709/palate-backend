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
    @Query("SELECT FUNCTION('HOUR', o.createdAt) as hour, COUNT(o) FROM Order o " +
            "WHERE o.status = 'PAID' AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('HOUR', o.createdAt)")
    List<Object[]> getPaidOrdersVolumeByHour(@Param("start") Instant start, @Param("end") Instant end);

    // 3. Operational: Total Orders (All statuses, for kitchen/waiter load)
    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE o.createdAt BETWEEN :start AND :end")
    long getTotalOrderVolume(@Param("start") Instant start, @Param("end") Instant end);

    // 4. Operational: Peak Hour (All statuses)
    @Query("SELECT FUNCTION('HOUR', o.createdAt) as hour, COUNT(o) as total " +
            "FROM Order o WHERE o.createdAt BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('HOUR', o.createdAt) ORDER BY total DESC")
    List<Object[]> getPeakOrderHour(@Param("start") Instant start, @Param("end") Instant end);

}