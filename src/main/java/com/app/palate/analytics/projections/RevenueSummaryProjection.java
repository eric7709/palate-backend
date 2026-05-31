package com.app.palate.analytics.projections;

public interface RevenueSummaryProjection {
    Double getTotalRevenue();
    Double getAvgOrderValue();
    Long getTotalOrders();
}