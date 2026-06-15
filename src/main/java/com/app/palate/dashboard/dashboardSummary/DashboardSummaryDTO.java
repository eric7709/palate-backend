package com.app.palate.dashboard.dashboardSummary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private double totalRevenue;
    private double previousRevenue;
    private ActiveOrdersDTO activeOrders;
    private MenuItemsDTO menuItems;
    private FloorStatusDTO floorStatus;
}