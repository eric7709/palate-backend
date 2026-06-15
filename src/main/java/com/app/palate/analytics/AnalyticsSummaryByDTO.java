package com.app.palate.analytics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

import org.hibernate.annotations.Imported;

@Data
@Builder
@Imported // ← tells Hibernate 6 to register this class for JPQL instantiation

public class AnalyticsSummaryByDTO {
    // KPIs 
    private Double totalRevenue;
    private Long totalOrders;
    private Double averageOrderValue;
    private Double averageItemsPerOrder;
    private Long cancelledOrders;
    private Double cancellationRate;
    private Long newCustomers;
    private Long returningCustomers;
    // Top by sales
    private List<AccountSalesDTO> topWaitersBySales;
    private List<AccountSalesDTO> topCashiersBySales;
    private List<TableSalesDTO> topTablesBySales;
    private List<CategorySalesDTO> topCategoriesBySales;
    private List<MenuItemSalesDTO> topMenuItemsBySales;
    private List<CustomerSalesDTO> topCustomersBySales;

    // Top by count
    private List<AccountSalesDTO> topWaitersByCount;
    private List<AccountSalesDTO> topCashiersByCount;
    private List<TableSalesDTO> topTablesByCount;
    private List<CategorySalesDTO> topCategoriesByCount;
    private List<MenuItemSalesDTO> topMenuItemsByCount;
    private List<CustomerSalesDTO> topCustomersByCount;
    private List<CustomerSalesDTO> topCustomersByFrequency;

    // Least by sales
    private List<AccountSalesDTO> leastWaitersBySales;
    private List<AccountSalesDTO> leastCashiersBySales;
    private List<TableSalesDTO> leastTablesBySales;
    private List<CategorySalesDTO> leastCategoriesBySales;
    private List<MenuItemSalesDTO> leastMenuItemsBySales;

    // Least by count
    private List<AccountSalesDTO> leastWaitersByCount;
    private List<AccountSalesDTO> leastCashiersByCount;
    private List<TableSalesDTO> leastTablesByCount;
    private List<CategorySalesDTO> leastCategoriesByCount;
    private List<MenuItemSalesDTO> leastMenuItemsByCount;

    // Time-based
    private List<DaySalesDTO> salesByDay;
    private List<HourSalesDTO> salesByHour;
    private List<DailyRevenueDTO> revenueOverTime;

    // Order composition
    private Long takeOutCount;
    private Double takeOutRevenue;
    private Long dineInCount;
    private Double dineInRevenue;

    // Top by sales
    private List<RoomSalesDTO> topRoomsBySales;
    // Top by count
    private List<RoomSalesDTO> topRoomsByCount;
    // Least by sales
    private List<RoomSalesDTO> leastRoomsBySales;
    // Least by count
    private List<RoomSalesDTO> leastRoomsByCount;
}
