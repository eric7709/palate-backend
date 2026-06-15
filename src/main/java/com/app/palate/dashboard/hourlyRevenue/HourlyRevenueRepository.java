package com.app.palate.dashboard.hourlyRevenue;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.palate.order.Order;
import com.app.palate.order.OrderStatus;

public interface HourlyRevenueRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT new com.app.palate.dashboard.hourlyRevenue.HourlyRevenueData(
                CASE WHEN o.createdAt >= :currentStart THEN 'CURRENT' ELSE 'PREVIOUS' END,
                o.createdAt,
                o.total
            )
            FROM Order o
            WHERE o.status = :status
            AND o.createdAt BETWEEN :previousStart AND :currentEnd
            """)
    List<HourlyRevenueData> getCombinedHourlyRevenue(
            @Param("status") OrderStatus status,
            @Param("previousStart") Instant previousStart,
            @Param("currentStart") Instant currentStart,
            @Param("currentEnd") Instant currentEnd);
}