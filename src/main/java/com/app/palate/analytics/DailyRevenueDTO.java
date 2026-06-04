package com.app.palate.analytics;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDate;

import org.hibernate.annotations.Imported;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Imported   // ← tells Hibernate 6 to register this class for JPQL instantiation

public class DailyRevenueDTO {
    private LocalDate date;
    private Long orderCount;
    private Double totalSales;
}
 