package com.app.palate.dashboard.topCategories;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.palate.orderItem.OrderItem;
import com.app.palate.order.OrderStatus;

public interface TopCategoryRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            SELECT new com.app.palate.dashboard.topCategories.CategoryRevenueData(
                mi.category.id,
                mi.category.name,
                SUM(oi.price * oi.quantity)
            )
            FROM OrderItem oi
            JOIN oi.menuItem mi
            WHERE oi.order.status = :status
            AND oi.order.createdAt BETWEEN :start AND :end
            GROUP BY mi.category.id, mi.category.name
            ORDER BY SUM(oi.price * oi.quantity) DESC
            """)
    List<CategoryRevenueData> findTopCategoriesByRevenue(
            @Param("status") OrderStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);
}