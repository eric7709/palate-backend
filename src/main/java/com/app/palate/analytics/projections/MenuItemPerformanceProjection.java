package com.app.palate.analytics.projections;

public interface MenuItemPerformanceProjection {
    Long getItemId();
    String getItemName();
    Long getTotalQuantity();
    Double getTotalRevenue();
}