package com.app.palate.analytics.dto;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RevenueOverTimeDTO {
    private Instant period;
    private Double revenue;
    private Long orderCount;
}
