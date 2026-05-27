package com.app.palate.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderSummaryResponse {
    private long totalOrders;
    private long pending;
    private long completed;
    private long cancelled;
    private long paid;
    private long preparing;
    private double totalAmount;
}