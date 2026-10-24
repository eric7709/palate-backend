package com.app.palate.dashboard.dashboardSummary.services;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.dashboard.dashboardSummary.DashboardSummaryRepository;
import com.app.palate.dashboard.dashboardSummary.dto.ActiveOrdersDTO;
import com.app.palate.dashboard.dashboardSummary.dto.DashboardSummaryDTO;
import com.app.palate.dashboard.dashboardSummary.dto.FloorStatusDTO;
import com.app.palate.dashboard.dashboardSummary.dto.MenuItemsDTO;
import com.app.palate.dashboard.dashboardSummary.dto.RevenueSummary;
import com.app.palate.dashboard.utils.DashboardPeriods;
import com.app.palate.menuItem.MenuItemRepository;
import com.app.palate.menuItem.enums.MenuItemStatus;
import com.app.palate.order.enums.OrderStatus;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.room.RoomRepository;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

/** Revenue for the period against the one before it, plus order, menu item, table and room counts. */
@Service
@RequiredArgsConstructor
public class GetDashboardSummary {
    private final DashboardSummaryRepository summaryRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final RoomRepository roomRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    @Transactional(readOnly = true)
    public DashboardSummaryDTO get(DashboardPeriod period) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime currentStartZoned = DashboardPeriods.startOf(period, now);
        ZonedDateTime previousStartZoned = DashboardPeriods.previousStartOf(period, currentStartZoned);

        Instant currentStart = currentStartZoned.toInstant();
        Instant currentEnd = now.toInstant();
        Instant previousStart = previousStartZoned.toInstant();

        // Current and previous revenue come back from one query
        List<RevenueSummary> results = summaryRepository.sumRevenueForSummary(
                OrderStatus.PAID,
                previousStart,
                currentStart,
                currentEnd);

        double currentRevenue = 0.0;
        double previousRevenue = 0.0;

        for (RevenueSummary r : results) {
            if (r.total() != null) {
                if ("CURRENT".equals(r.periodFlag())) {
                    currentRevenue = r.total();
                } else if ("PREVIOUS".equals(r.periodFlag())) {
                    previousRevenue = r.total();
                }
            }
        }

        ActiveOrdersDTO activeOrders = new ActiveOrdersDTO(
                summaryRepository.countByStatus(OrderStatus.PENDING),
                summaryRepository.countByStatus(OrderStatus.PAID));

        MenuItemsDTO menuItems = new MenuItemsDTO(
                menuItemRepository.countByStatus(MenuItemStatus.AVAILABLE),
                menuItemRepository.countByStatus(MenuItemStatus.UNAVAILABLE));

        FloorStatusDTO floorStatus = new FloorStatusDTO(tableRepository.count(), roomRepository.count());

        return new DashboardSummaryDTO(currentRevenue, previousRevenue, activeOrders, menuItems, floorStatus);
    }
}
