package com.app.palate.dashboard.dashboardSummary;

/**
 * Clean data carrier matching the custom repository constructor signature perfectly.
 */
public record RevenueSummary(
    String periodFlag,   // Matches r.periodFlag() in DashboardSummaryService
    Double total         // Matches the Double output signature of the database SUM() operation
) {}