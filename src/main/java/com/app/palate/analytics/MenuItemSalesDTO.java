package com.app.palate.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Imported;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Imported   // ← tells Hibernate 6 to register this class for JPQL instantiation
public class MenuItemSalesDTO {
    private Long id;
    private String name;
    private String categoryName;
    private Double totalSales;
    private Long totalQuantity;
}