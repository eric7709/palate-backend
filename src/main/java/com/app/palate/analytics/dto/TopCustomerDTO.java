package com.app.palate.analytics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopCustomerDTO {
    private Long customerId;
    private String customerName;
    private Long orderCount;
    private Double totalSpent;
}