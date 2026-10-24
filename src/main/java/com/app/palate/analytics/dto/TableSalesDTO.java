package com.app.palate.analytics.dto;

import org.hibernate.annotations.Imported;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Imported // registers this class for JPQL instantiation (Hibernate 6)
public class TableSalesDTO {
    private Long id;
    private String tableName;
    private Integer tableNumber;
    private Double totalSales;
    private Long orderCount;
}
