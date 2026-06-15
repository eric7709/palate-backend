package com.app.palate.orderItem;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.palate.order.OrderStatus;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi.menuItem.id, oi.menuItem.name, SUM(oi.price * oi.quantity) as revenue " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.status = :status AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY oi.menuItem.id, oi.menuItem.name " +
            "ORDER BY revenue DESC " +
            "LIMIT :limit")
    List<Object[]> sumRevenueByMenuItem(@Param("status") OrderStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("limit") int limit);

    @Query("SELECT c.id, c.name, SUM(oi.price * oi.quantity) as revenue " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.menuItem mi " +
            "LEFT JOIN mi.category c " +
            "WHERE o.status = :status AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY c.id, c.name " +
            "ORDER BY revenue DESC " +
            "LIMIT :limit")
    List<Object[]> sumRevenueByCategory(@Param("status") OrderStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("limit") int limit);

    @Query("SELECT oi.menuItem.id, oi.menuItem.name, SUM(oi.price * oi.quantity) as revenue " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.status = :status AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY oi.menuItem.id, oi.menuItem.name " +
            "ORDER BY revenue DESC")
    List<Object[]> sumRevenueByMenuItem(@Param("status") OrderStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);

    // Top Categories (by revenue)
    @Query("SELECT c.id, c.name, SUM(oi.price * oi.quantity) as revenue " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.menuItem mi " +
            "LEFT JOIN mi.category c " +
            "WHERE o.status = :status AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY c.id, c.name " +
            "ORDER BY revenue DESC")
    List<Object[]> sumRevenueByCategory(@Param("status") OrderStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);
}