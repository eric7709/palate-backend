package com.app.palate.analytics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TableActivityDTO {
    private Long tableId;
    private String tableName;
    private Integer tableNumber;
    private Long orderCount;
    private Double totalRevenue;
}