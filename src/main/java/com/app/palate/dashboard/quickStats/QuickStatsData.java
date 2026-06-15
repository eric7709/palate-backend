package com.app.palate.dashboard.quickStats;

public record QuickStatsData(
    long totalOrders,
    Double totalRevenue,
    long totalCustomers,
    long repeatCustomers
) {}