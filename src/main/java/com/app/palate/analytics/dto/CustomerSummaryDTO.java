package com.app.palate.analytics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerSummaryDTO {
    private Long newCustomers;
    private Long returningCustomers;
}