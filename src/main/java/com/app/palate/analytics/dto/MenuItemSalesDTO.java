package com.app.palate.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Imported;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Imported // registers this class for JPQL instantiation (Hibernate 6)
public class MenuItemSalesDTO {
    private Long id;
    private String name;
    private String categoryName;
    private Double totalSales;
    private Long totalQuantity;
}
