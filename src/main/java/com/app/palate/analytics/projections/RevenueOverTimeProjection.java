package com.app.palate.analytics.projections;

import java.time.Instant;

public interface RevenueOverTimeProjection {
    Instant getPeriod();
    Double getRevenue();
    Long getOrderCount();
}
