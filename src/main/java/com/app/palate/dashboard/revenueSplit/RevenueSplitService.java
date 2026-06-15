package com.app.palate.dashboard.revenueSplit;

import com.app.palate.order.OrderStatus;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RevenueSplitService {

    private final RevenueSplitRepository revenueSplitRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    public RevenueSplitResponse getRevenueSplit(DashboardPeriod period) {

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime start = getStartOfPeriod(period, now);

        List<RevenueByType> results = revenueSplitRepository.sumRevenueByOrderType(
                OrderStatus.PAID,
                start.toInstant(),
                now.toInstant()
        );

        Map<String, BigDecimal> map = new HashMap<>();
        map.put("RESTAURANT", BigDecimal.ZERO);
        map.put("ROOM_SERVICE", BigDecimal.ZERO);

        for (RevenueByType r : results) {
            // Converts Double safely into a clean BigDecimal object instance
            BigDecimal value = (r.total() != null) ? BigDecimal.valueOf(r.total()) : BigDecimal.ZERO;
            map.put(r.type(), value);
        }

        BigDecimal restaurant = map.get("RESTAURANT");
        BigDecimal roomService = map.get("ROOM_SERVICE");
        BigDecimal totalRevenue = restaurant.add(roomService);

        int restaurantPercent = percentage(restaurant, totalRevenue);
        int roomServicePercent = percentage(roomService, totalRevenue);

        List<RevenueSplitResponse.RevenueSplitItem> data = List.of(
                new RevenueSplitResponse.RevenueSplitItem(
                        "Restaurant",
                        formatCurrency(restaurant),
                        restaurantPercent
                ),
                new RevenueSplitResponse.RevenueSplitItem(
                        "Room service",
                        formatCurrency(roomService),
                        roomServicePercent
                )
        );

        return new RevenueSplitResponse(data, restaurantPercent);
    }

    private int percentage(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return 0;

        return part
                .divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .intValue();
    }

    private ZonedDateTime getStartOfPeriod(DashboardPeriod period, ZonedDateTime now) {
        return switch (period) {
            case TODAY -> now.toLocalDate().atStartOfDay(zoneId);
            case THIS_WEEK -> now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay(zoneId);
            case THIS_MONTH -> now.withDayOfMonth(1).toLocalDate().atStartOfDay(zoneId);
            case THIS_YEAR -> now.withDayOfYear(1).toLocalDate().atStartOfDay(zoneId);
        };
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₦0";
        long value = amount.longValue();
        if (value >= 1_000_000)
            return "₦" + (value / 1_000_000) + "M";
        if (value >= 1_000)
            return "₦" + (value / 1_000) + "k";
        return "₦" + value;
    }
}