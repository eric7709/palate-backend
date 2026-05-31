package com.app.palate.analytics.projections;

public interface OrdersByStatusProjection {
    String getStatus();
    Long getCount();
}