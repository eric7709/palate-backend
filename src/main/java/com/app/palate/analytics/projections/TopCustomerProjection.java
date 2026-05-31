package com.app.palate.analytics.projections;

public interface TopCustomerProjection {
    Long getCustomerId();
    String getCustomerName();
    Long getOrderCount();
    Double getTotalSpent();
}
