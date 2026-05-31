package com.app.palate.analytics.projections;


public interface StaffPerformanceProjection {
    Long getStaffId();
    String getStaffName();
    Long getOrderCount();
    Double getTotalValue(); // If 'total' in Order is double, use Double here
}