package com.app.palate.dashboard.topCategories.dto;

public record CategoryRevenueData(
    Long categoryId,
    String categoryName,
    Double totalRevenue
) {}
