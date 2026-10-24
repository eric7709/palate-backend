package com.app.palate.dashboard.quickStats.dto;

public record QuickStatsData(
    long totalOrders,
    Double totalRevenue,
    long totalCustomers,
    long repeatCustomers
) {}
