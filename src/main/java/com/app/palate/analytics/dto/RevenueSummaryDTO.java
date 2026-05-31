package com.app.palate.analytics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RevenueSummaryDTO {
    private Double totalRevenue;
    private Double avgOrderValue;
    private Long totalOrders;
}