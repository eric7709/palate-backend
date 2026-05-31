package com.app.palate.analytics.projections;

public interface PeakHourProjection {
    Integer getHour();
    Long getCount();
}