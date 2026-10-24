package com.app.palate.dashboard.topMenuItems.dto;

public record MenuItemRevenueData(
    Long menuItemId,
    String menuItemName,
    Double totalRevenue
) {}
