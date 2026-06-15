package com.app.palate.dashboard.topMenuItems;

public record MenuItemRevenueData(
    Long menuItemId, 
    String menuItemName, 
    Double totalRevenue
) {}