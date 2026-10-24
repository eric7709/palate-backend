package com.app.palate.dashboard.hourlyRevenue.dto;

import java.time.Instant;

public record HourlyRevenueData(
    String periodFlag,
    Instant createdAt,
    Double total
) {}
