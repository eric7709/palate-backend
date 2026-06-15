package com.app.palate.menuItem;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long>, JpaSpecificationExecutor<MenuItem> {
    boolean existsByNameIgnoreCase(String name);

    long countByStatus(MenuItemStatus status);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
                SELECT new com.app.palate.menuItem.TopMenuItemDTO(
                    oi.menuItem.id,
                    oi.menuItem.name,
                    SUM(oi.price * oi.quantity)
                )
                FROM OrderItem oi
                WHERE oi.order.createdAt BETWEEN :startDate AND :endDate
                GROUP BY oi.menuItem.id, oi.menuItem.name
                ORDER BY SUM(oi.price * oi.quantity) DESC
            """)
    List<TopMenuItemDTO> findTopMenuItems(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT LOWER(m.name) FROM MenuItem m WHERE LOWER(m.name) IN :names")
    List<String> findExistingNames(@Param("names") List<String> names);

}
