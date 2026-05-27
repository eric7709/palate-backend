package com.app.palate.order;

public interface OrderSummaryProjection {
    Long getTotalOrders();
    Long getPending();
    Long getCompleted();
    Long getCancelled();
    Long getPreparing();
    Long getPaid(); // Add this
    Double getTotalAmount();
}