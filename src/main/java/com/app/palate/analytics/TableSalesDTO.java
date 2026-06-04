package com.app.palate.analytics;
 
import org.hibernate.annotations.Imported;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Imported   // ← tells Hibernate 6 to register this class for JPQL instantiation

public class TableSalesDTO {
    private Long id;
    private String tableName;
    private Integer tableNumber;
    private Double totalSales;
    private Long orderCount;
}