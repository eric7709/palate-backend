package com.app.palate.dashboard;

public record DashboardStats(
    double totalRevenue,
    double revenueGrowthPercent,
    long totalOrders,
    double ordersGrowthPercent,
    long totalCustomers,
    double customersGrowthPercent,
    double avgOrderValue,
    double avgOrderGrowthPercent
) {}