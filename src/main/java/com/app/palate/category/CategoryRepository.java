package com.app.palate.category;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("SELECT LOWER(c.name) FROM Category c WHERE LOWER(c.name) IN :names")
    List<String> findExistingNamesIgnoreCase(@Param("names") List<String> names);

    // Existing method
    @Query("""
                SELECT new com.app.palate.category.CategoryResponseDTO(
                    c.id,
                    c.name,
                    c.description,
                    c.createdAt,
                    c.status,
                    COUNT(m)
                )
                FROM Category c
                LEFT JOIN c.menuItems m
                GROUP BY
                    c.id,
                    c.name,
                    c.description,
                    c.createdAt,
                    c.status
                ORDER BY c.createdAt DESC
            """)
    List<CategoryResponseDTO> findAllWithMenuItemCount();

    // Top categories by sales (revenue) within date range
    @Query("""
                SELECT new com.app.palate.category.TopCategoryDTO(
                    c.id,
                    c.name,
                    SUM(oi.price * oi.quantity)
                )
                FROM OrderItem oi
                JOIN oi.menuItem m
                JOIN m.category c
                WHERE oi.order.createdAt BETWEEN :startDate AND :endDate
                GROUP BY c.id, c.name
                ORDER BY SUM(oi.price * oi.quantity) DESC
            """)
    List<TopCategoryDTO> findTopCategoriesBySales(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}