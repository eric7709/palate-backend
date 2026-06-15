package com.app.palate.dashboard.topMenuItems;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.palate.orderItem.OrderItem;
import com.app.palate.order.OrderStatus;

public interface TopMenuItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            SELECT new com.app.palate.dashboard.topMenuItems.MenuItemRevenueData(
                mi.id,
                mi.name,
                SUM(oi.price * oi.quantity)
            )
            FROM OrderItem oi
            JOIN oi.menuItem mi
            WHERE oi.order.status = :status
            AND oi.order.createdAt BETWEEN :start AND :end
            GROUP BY mi.id, mi.name
            ORDER BY SUM(oi.price * oi.quantity) DESC
            """)
    List<MenuItemRevenueData> findTopMenuItemsByRevenue(
            @Param("status") OrderStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);
}