package com.app.palate.dashboard.dashboardSummary;

import com.app.palate.menuItem.MenuItemRepository;
import com.app.palate.menuItem.MenuItemStatus;
import com.app.palate.order.OrderStatus;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.room.RoomRepository;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardSummaryService {

    private final DashboardSummaryRepository summaryRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final RoomRepository roomRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    public DashboardSummaryDTO getSummary(DashboardPeriod period) {
        // 1. Calculate explicit current and historical time milestones
        ZonedDateTime nowZoned = ZonedDateTime.now(zoneId);
        ZonedDateTime currentStartZoned = getStartOfPeriodZoned(period, nowZoned);
        ZonedDateTime previousStartZoned = getStartOfPreviousPeriodZoned(period, currentStartZoned);
        
        Instant currentStart = currentStartZoned.toInstant();
        Instant currentEnd = nowZoned.toInstant();
        Instant previousStart = previousStartZoned.toInstant();

        // 2. Query combined window metrics inside a single database trip
        List<RevenueSummary> results = summaryRepository.sumRevenueForSummary(
                OrderStatus.PAID,
                previousStart,
                currentStart,
                currentEnd
        );

        Double currentRevenue = 0.0;
        Double previousRevenue = 0.0;

        for (RevenueSummary r : results) {
            if (r.total() != null) {
                if ("CURRENT".equals(r.periodFlag())) {
                    currentRevenue = r.total();
                } else if ("PREVIOUS".equals(r.periodFlag())) {
                    previousRevenue = r.total();
                }
            }
        }

        // 3. Gather operation statistics counters
        long pendingOrders = summaryRepository.countByStatus(OrderStatus.PENDING);
        long paidOrders = summaryRepository.countByStatus(OrderStatus.PAID);
        ActiveOrdersDTO activeOrders = new ActiveOrdersDTO(pendingOrders, paidOrders);

        // 4. Gather menu catalog items tracking metrics 
        long activeMenuItems = menuItemRepository.countByStatus(MenuItemStatus.AVAILABLE);
        long unavailableMenuItems = menuItemRepository.countByStatus(MenuItemStatus.UNAVAILABLE);
        MenuItemsDTO menuItems = new MenuItemsDTO(activeMenuItems, unavailableMenuItems);

        // 5. Gather floor plan capacity metadata counters
        long totalTables = tableRepository.count();
        long totalRooms = roomRepository.count();
        FloorStatusDTO floorStatus = new FloorStatusDTO(totalTables, totalRooms);

        return new DashboardSummaryDTO(
                currentRevenue, 
                previousRevenue, 
                activeOrders, 
                menuItems, 
                floorStatus
        );
    }

    private ZonedDateTime getStartOfPeriodZoned(DashboardPeriod period, ZonedDateTime now) {
        return switch (period) {
            case TODAY -> now.toLocalDate().atStartOfDay(zoneId);
            case THIS_WEEK -> now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay(zoneId);
            case THIS_MONTH -> now.withDayOfMonth(1).toLocalDate().atStartOfDay(zoneId);
            case THIS_YEAR -> now.withDayOfYear(1).toLocalDate().atStartOfDay(zoneId);
        };
    }

    private ZonedDateTime getStartOfPreviousPeriodZoned(DashboardPeriod period, ZonedDateTime currentStart) {
        return switch (period) {
            case TODAY -> currentStart.minusDays(1);
            case THIS_WEEK -> currentStart.minusWeeks(1);
            case THIS_MONTH -> currentStart.minusMonths(1);
            case THIS_YEAR -> currentStart.minusYears(1);
        };
    }
}