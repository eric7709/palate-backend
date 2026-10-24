package com.app.palate.analytics.dto;

import org.hibernate.annotations.Imported;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Imported // registers this class for JPQL instantiation (Hibernate 6)
public class CustomerSalesDTO {
    private Long id;
    private String name;
    private String phoneNumber;
    private Double totalSales;
    private Long orderCount;
}
