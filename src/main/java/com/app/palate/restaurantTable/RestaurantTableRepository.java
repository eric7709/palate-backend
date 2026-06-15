package com.app.palate.restaurantTable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantTableRepository
        extends JpaRepository<RestaurantTable, Long>, JpaSpecificationExecutor<RestaurantTable> {
    RestaurantTable findByTableNameContainingIgnoreCase(String tableName);

    Optional<RestaurantTable> findByTableName(String tableName);

    RestaurantTable findByTableNumber(Integer tableNumber);

    @Query("""
                SELECT new com.app.palate.restaurantTable.RestaurantTopTableDTO(
                    o.table.id,
                    o.table.tableName,
                    CAST(o.table.tableNumber AS string),
                    SUM(o.total)
                )
                FROM Order o
                WHERE o.createdAt BETWEEN :startDate AND :endDate
                GROUP BY o.table.id, o.table.tableName, o.table.tableNumber
                ORDER BY SUM(o.total) DESC
            """)
    List<RestaurantTopTableDTO> findTopTables(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t.tableName FROM RestaurantTable t WHERE t.tableName IN :names")
    List<String> findExistingNames(@Param("names") List<String> names);

    Optional<RestaurantTable> findByQrCode(String qrCode);

    @Query("SELECT t.tableNumber FROM RestaurantTable t WHERE t.tableNumber IN :numbers")
    List<Integer> findExistingNumbers(@Param("numbers") List<Integer> numbers);

    @Query("SELECT t.tableNumber FROM RestaurantTable t WHERE t.tableNumber IN :numbers")
    List<Integer> findExistingTableNumbers(@Param("numbers") List<Integer> numbers);

    List<RestaurantTable> findByCashierId(Long cashierId);

    List<RestaurantTable> findByWaiterId(Long waiterId);
}