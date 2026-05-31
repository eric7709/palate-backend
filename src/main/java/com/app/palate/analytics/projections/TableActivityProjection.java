package com.app.palate.analytics.projections;

public interface TableActivityProjection {
    Long getTableId();
    String getTableName();
    Integer getTableNumber();
    Long getOrderCount();
    Double getTotalRevenue();
}