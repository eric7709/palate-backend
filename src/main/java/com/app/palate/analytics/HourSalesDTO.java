package com.app.palate.analytics;
 
import org.hibernate.annotations.Imported;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Imported   // ← tells Hibernate 6 to register this class for JPQL instantiation

public class HourSalesDTO {
    private Integer hour;
    private Long orderCount;
    private Double totalSales;
}
 