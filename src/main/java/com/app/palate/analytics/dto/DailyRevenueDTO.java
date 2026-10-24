package com.app.palate.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import org.hibernate.annotations.Imported;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Imported // registers this class for JPQL instantiation (Hibernate 6)
public class DailyRevenueDTO {
    private LocalDate date;
    private Long orderCount;
    private Double totalSales;
}
 
