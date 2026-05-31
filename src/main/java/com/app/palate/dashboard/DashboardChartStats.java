package com.app.palate.dashboard;

import java.util.List;

public record DashboardChartStats(
    List<DailyRevenue>  salesOverview,
    List<HourlyRevenue> revenueByHour
) {
    public record DailyRevenue(
        String date,    // "2024-11-01"
        double revenue,
        long   orderCount
    ) {}

    public record HourlyRevenue(
        int    hour,    // 0-23
        double revenue,
        long   orderCount
    ) {}
}