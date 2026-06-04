package com.app.palate.analytics;
 
import org.hibernate.annotations.Imported;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Imported   // ← tells Hibernate 6 to register this class for JPQL instantiation

public class CustomerSalesDTO {
    private Long id;
    private String name;
    private String phoneNumber;
    private Double totalSales;
    private Long orderCount;
}