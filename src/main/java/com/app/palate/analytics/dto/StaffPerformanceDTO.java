package com.app.palate.analytics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StaffPerformanceDTO {
    private Long staffId;
    private String staffName;
    private Long orderCount;
    private Double totalValue;
}
