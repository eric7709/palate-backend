package com.app.palate.dashboard.revenueSplit;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.palate.order.Order;
import com.app.palate.order.OrderStatus;

public interface RevenueSplitRepository extends JpaRepository<Order, Long> {
    @Query("""
            SELECT new com.app.palate.dashboard.revenueSplit.RevenueByType(
              CASE WHEN o.room IS NOT NULL THEN 'ROOM_SERVICE' ELSE 'RESTAURANT' END,
              SUM(o.total)
            )
            FROM Order o
            WHERE o.status = :status
            AND o.createdAt BETWEEN :start AND :end
            GROUP BY CASE WHEN o.room IS NOT NULL THEN 'ROOM_SERVICE' ELSE 'RESTAURANT' END
            """)
    List<RevenueByType> sumRevenueByOrderType(
            @Param("status") OrderStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end);
}
