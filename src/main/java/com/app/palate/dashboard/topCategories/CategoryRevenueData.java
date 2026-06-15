package com.app.palate.dashboard.topCategories;

public record CategoryRevenueData(
    Long categoryId, 
    String categoryName, 
    Double totalRevenue
) {}