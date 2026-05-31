package com.app.palate.analytics.projections;

public interface CancelledItemProjection {
    Long getItemId();
    String getItemName();
    Long getCancelCount();
}