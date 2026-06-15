package com.app.palate.order;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.createdAt <= :cutoff")
    List<Order> findStaleOrders(@Param("cutoff") Instant cutoff);
    // Inside your OrderRepository.java

    @Query("""
                SELECT
                    COUNT(o)                                                      AS totalOrders,
                    SUM(CASE WHEN o.status = 'PENDING'   THEN 1 ELSE 0 END)  AS pending,
                    SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END)  AS completed,
                    SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END)  AS cancelled,
                    SUM(CASE WHEN o.status = 'PREPARING' THEN 1 ELSE 0 END)  AS preparing,
                    SUM(CASE WHEN o.status = 'PAID'      THEN 1 ELSE 0 END)  AS paid,
                    SUM(CASE WHEN o.status = 'PAID'      THEN o.total ELSE 0 END) AS totalAmount
                FROM Order o
                WHERE o.createdAt >= :startOfDay AND o.createdAt < :endOfDay
                AND (:waiterId IS NULL OR o.waiter.id = :waiterId)
                AND (:cashierId IS NULL OR o.cashier.id = :cashierId)
            """)
    OrderSummaryProjection getSummaryByDate(
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay,
            @Param("waiterId") Long waiterId,
            @Param("cashierId") Long cashierId);

    @Query("""
            SELECT o
            FROM Order o
            WHERE o.customer.id = :customerId
              AND o.createdAt BETWEEN :start AND :end
            ORDER BY o.createdAt DESC
            """)
    List<Order> fetchCustomerOrdersToday(
            @Param("customerId") Long customerId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    // --- FINANCIALS & QUANTITY ---
    @Query("SELECT COUNT(o), SUM(o.total) FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    Object[] getGlobalTotals(@Param("start") Instant start, @Param("end") Instant end);

    // --- TIME-OF-DAY SHIFTS ---
    @Query("""
                SELECT
                    CASE
                        WHEN function('date_part', 'hour', o.createdAt) BETWEEN 6 AND 11 THEN 'MORNING'
                        WHEN function('date_part', 'hour', o.createdAt) BETWEEN 12 AND 16 THEN 'AFTERNOON'
                        ELSE 'EVENING'
                    END,
                    COUNT(o),
                    SUM(o.total)
                FROM Order o
                WHERE o.createdAt BETWEEN :start AND :end
                GROUP BY 1
                ORDER BY 3 DESC
            """)
    List<Object[]> getSalesByShift(@Param("start") Instant start, @Param("end") Instant end);

    // --- DAY OF WEEK (0 = Sunday) ---
    // Sorting by 1 ASC here keeps the week in chronological order (Sun, Mon,
    // Tue...)
    @Query("""
                SELECT
                    function('date_part', 'dow', o.createdAt),
                    COUNT(o),
                    SUM(o.total)
                FROM Order o
                WHERE o.createdAt BETWEEN :start AND :end
                GROUP BY 1
                ORDER BY 1 ASC
            """)
    List<Object[]> getSalesByDayOfWeek(@Param("start") Instant start, @Param("end") Instant end);

    // --- MENU ITEMS & CATEGORIES ---
    @Query("""
                SELECT oi.menuItem.name, SUM(oi.quantity), SUM(oi.price * oi.quantity)
                FROM OrderItem oi
                WHERE oi.createdAt BETWEEN :start AND :end
                GROUP BY 1
                ORDER BY 3 DESC
            """)
    List<Object[]> getMenuItemStats(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
                SELECT mi.category.name, SUM(oi.quantity), SUM(oi.price * oi.quantity)
                FROM OrderItem oi
                JOIN oi.menuItem mi
                WHERE oi.createdAt BETWEEN :start AND :end
                GROUP BY 1
                ORDER BY 3 DESC
            """)
    List<Object[]> getCategoryStats(@Param("start") Instant start, @Param("end") Instant end);

    // --- STAFF & TABLES ---
    @Query("""
                SELECT o.waiter.firstName, COUNT(o), SUM(o.total)
                FROM Order o
                WHERE o.waiter IS NOT NULL AND o.createdAt BETWEEN :start AND :end
                GROUP BY 1
                ORDER BY 3 DESC
            """)
    List<Object[]> getWaiterStats(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
                SELECT o.table.tableName, COUNT(o), SUM(o.total)
                FROM Order o
                WHERE o.table IS NOT NULL AND o.createdAt BETWEEN :start AND :end
                GROUP BY 1
                ORDER BY 3 DESC
            """)
    List<Object[]> getTableStats(@Param("start") Instant start, @Param("end") Instant end);

    // --- CUSTOMERS ---
    @Query("""
                SELECT o.customer.name, COUNT(o), SUM(o.total)
                FROM Order o
                WHERE o.customer IS NOT NULL AND o.createdAt BETWEEN :start AND :end
                GROUP BY 1
                ORDER BY 3 DESC
            """)
    List<Object[]> getCustomerStats(@Param("start") Instant start, @Param("end") Instant end);

    long countByStatusNotIn(List<OrderStatus> statuses);

    Optional<Order> findByMonnifyReference(String monnifyReference);

}