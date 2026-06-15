package com.app.palate.dashboard.dashboardSummary;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.palate.order.Order;
import com.app.palate.order.OrderStatus;

public interface DashboardSummaryRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT new com.app.palate.dashboard.dashboardSummary.RevenueSummary(
                sub.period,
                COALESCE(SUM(sub.totalPrice), 0.0)
            )
            FROM (
                SELECT 
                    CASE WHEN o.createdAt >= :currentStart THEN 'CURRENT' ELSE 'PREVIOUS' END as period,
                    o.total as totalPrice
                FROM Order o
                WHERE o.status = :status
                AND o.createdAt BETWEEN :previousStart AND :currentEnd
            ) sub
            GROUP BY sub.period
            """)
    List<RevenueSummary> sumRevenueForSummary(
            @Param("status") OrderStatus status,
            @Param("previousStart") Instant previousStart,
            @Param("currentStart") Instant currentStart,
            @Param("currentEnd") Instant currentEnd);

    long countByStatus(OrderStatus status);
}