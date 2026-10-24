package com.app.palate.dashboard.revenueSplit.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.dashboard.revenueSplit.RevenueSplitRepository;
import com.app.palate.dashboard.revenueSplit.dto.RevenueByType;
import com.app.palate.dashboard.revenueSplit.dto.RevenueSplitResponse;
import com.app.palate.dashboard.utils.CurrencyFormatter;
import com.app.palate.dashboard.utils.DashboardPeriods;
import com.app.palate.order.enums.OrderStatus;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

/** Splits the period's paid revenue between restaurant orders and room-service orders. */
@Service
@RequiredArgsConstructor
public class GetRevenueSplit {
    private static final String RESTAURANT = "RESTAURANT";
    private static final String ROOM_SERVICE = "ROOM_SERVICE";

    private final RevenueSplitRepository revenueSplitRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    @Transactional(readOnly = true)
    public RevenueSplitResponse get(DashboardPeriod period) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime start = DashboardPeriods.startOf(period, now);

        List<RevenueByType> results = revenueSplitRepository.sumRevenueByOrderType(
                OrderStatus.PAID,
                start.toInstant(),
                now.toInstant());

        Map<String, BigDecimal> byType = new HashMap<>();
        byType.put(RESTAURANT, BigDecimal.ZERO);
        byType.put(ROOM_SERVICE, BigDecimal.ZERO);

        for (RevenueByType r : results) {
            byType.put(r.type(), r.total() != null ? BigDecimal.valueOf(r.total()) : BigDecimal.ZERO);
        }

        BigDecimal restaurant = byType.get(RESTAURANT);
        BigDecimal roomService = byType.get(ROOM_SERVICE);
        BigDecimal totalRevenue = restaurant.add(roomService);

        int restaurantPercent = percentage(restaurant, totalRevenue);
        int roomServicePercent = percentage(roomService, totalRevenue);

        List<RevenueSplitResponse.RevenueSplitItem> data = List.of(
                new RevenueSplitResponse.RevenueSplitItem(
                        "Restaurant", CurrencyFormatter.format(restaurant), restaurantPercent),
                new RevenueSplitResponse.RevenueSplitItem(
                        "Room service", CurrencyFormatter.format(roomService), roomServicePercent));

        return new RevenueSplitResponse(data, restaurantPercent);
    }

    private int percentage(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0)
            return 0;

        return part
                .divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .intValue();
    }
}
