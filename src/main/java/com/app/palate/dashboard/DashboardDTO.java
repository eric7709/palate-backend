package com.app.palate.dashboard;

import java.util.List;
public record DashboardDTO(
    List<OrderHourDTO> hourlyVolume,
    List<TableAvgDTO> tableAverages,
    long totalOrdersToday,
    PeakHourDTO peakHour
) {}