package com.app.palate.dashboard.quickStats;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.palate.order.Order;
import com.app.palate.order.OrderStatus;

public interface QuickStatsRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT new com.app.palate.dashboard.quickStats.QuickStatsData(
                COUNT(o.id),
                SUM(o.total),
                COUNT(DISTINCT o.customer.id),
                (
                    SELECT COUNT(sub.customerId) FROM (
                        SELECT o2.customer.id as customerId 
                        FROM Order o2 
                        WHERE o2.status IN :statuses 
                        AND o2.createdAt BETWEEN :start AND :end 
                        AND o2.customer.id IS NOT NULL
                        GROUP BY o2.customer.id 
                        HAVING COUNT(o2.id) > 1
                    ) sub
                )
            )
            FROM Order o
            WHERE o.status IN :statuses
            AND o.createdAt BETWEEN :start AND :end
            """)
    Optional<QuickStatsData> getQuickStatsMetrics(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("start") Instant start,
            @Param("end") Instant end);
}