package com.app.palate.analytics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuItemPerformanceDTO {
    private Long itemId;
    private String itemName;
    private Long totalQuantity;
    private Double totalRevenue;
}