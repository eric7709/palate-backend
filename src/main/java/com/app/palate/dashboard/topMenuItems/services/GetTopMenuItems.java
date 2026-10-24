package com.app.palate.dashboard.topMenuItems.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.dashboard.topMenuItems.TopMenuItemRepository;
import com.app.palate.dashboard.topMenuItems.dto.Item;
import com.app.palate.dashboard.topMenuItems.dto.MenuItemRevenueData;
import com.app.palate.dashboard.topMenuItems.dto.TopMenuItemResponse;
import com.app.palate.dashboard.utils.CurrencyFormatter;
import com.app.palate.dashboard.utils.DashboardPeriods;
import com.app.palate.order.enums.OrderStatus;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

/** The menu items that earned the most paid revenue in the period (at most 5), each with its share of the top one. */
@Service
@RequiredArgsConstructor
public class GetTopMenuItems {
    private static final int MAX_ITEMS = 5;

    private final TopMenuItemRepository topMenuItemRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public TopMenuItemResponse get(DashboardPeriod period, int limit) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        Instant start = DashboardPeriods.startOf(period, now).toInstant();
        Instant end = now.toInstant();

        int cappedLimit = Math.min(limit, MAX_ITEMS);

        List<MenuItemRevenueData> results = topMenuItemRepository.findTopMenuItemsByRevenue(
                OrderStatus.PAID,
                start,
                end,
                PageRequest.of(0, cappedLimit));

        List<Item> items = new ArrayList<>();
        if (results.isEmpty()) {
            return new TopMenuItemResponse(items);
        }

        BigDecimal maxRevenue = results.stream()
                .map(r -> r.totalRevenue() != null ? BigDecimal.valueOf(r.totalRevenue()) : BigDecimal.ZERO)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);

        if (maxRevenue.compareTo(BigDecimal.ZERO) == 0) {
            maxRevenue = BigDecimal.ONE;
        }

        int rank = 1;
        for (MenuItemRevenueData data : results) {
            BigDecimal revenue = data.totalRevenue() != null
                    ? BigDecimal.valueOf(data.totalRevenue())
                    : BigDecimal.ZERO;

            int percent = revenue
                    .multiply(BigDecimal.valueOf(100))
                    .divide(maxRevenue, 0, RoundingMode.HALF_UP)
                    .intValue();

            items.add(new Item(
                    rank++,
                    data.menuItemName() != null ? data.menuItemName() : "Unknown Item",
                    CurrencyFormatter.format(revenue),
                    percent,
                    null));
        }

        return new TopMenuItemResponse(items);
    }
}
