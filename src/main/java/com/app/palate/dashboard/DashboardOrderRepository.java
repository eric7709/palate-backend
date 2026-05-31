package com.app.palate.dashboard;

import com.app.palate.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

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
}