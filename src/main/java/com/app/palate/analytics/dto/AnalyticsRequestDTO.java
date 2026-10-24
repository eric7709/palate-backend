package com.app.palate.analytics.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

import org.hibernate.annotations.Imported;

@Data
@Imported // registers this class for JPQL instantiation (Hibernate 6)
public class AnalyticsRequestDTO {

    @NotNull
    private LocalDate from;

    @NotNull
    private LocalDate to;

    private int limit = 10;
}
