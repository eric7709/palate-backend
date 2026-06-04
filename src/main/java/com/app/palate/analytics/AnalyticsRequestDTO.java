package com.app.palate.analytics;
 
import jakarta.validation.constraints.NotNull;
import lombok.Data;
 
import java.time.LocalDate;

import org.hibernate.annotations.Imported;
 
@Data
@Imported   // ← tells Hibernate 6 to register this class for JPQL instantiation

public class AnalyticsRequestDTO {
 
    @NotNull
    private LocalDate from;
 
    @NotNull
    private LocalDate to;
 
    private int limit = 10;
}