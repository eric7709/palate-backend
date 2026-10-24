package com.app.palate.dashboard.topCategories.services;

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

import com.app.palate.dashboard.topCategories.TopCategoryRepository;
import com.app.palate.dashboard.topCategories.dto.CategoryItem;
import com.app.palate.dashboard.topCategories.dto.CategoryRevenueData;
import com.app.palate.dashboard.topCategories.dto.TopCategoryResponse;
import com.app.palate.dashboard.utils.CurrencyFormatter;
import com.app.palate.dashboard.utils.DashboardPeriods;
import com.app.palate.order.enums.OrderStatus;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

/** The categories that earned the most paid revenue in the period (at most 5), each with its share of the top one. */
@Service
@RequiredArgsConstructor
public class GetTopCategories {
    private static final int MAX_ITEMS = 5;

    private final TopCategoryRepository topCategoryRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public TopCategoryResponse get(DashboardPeriod period, int limit) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        Instant start = DashboardPeriods.startOf(period, now).toInstant();
        Instant end = now.toInstant();

        int cappedLimit = Math.min(limit, MAX_ITEMS);

        List<CategoryRevenueData> results = topCategoryRepository.findTopCategoriesByRevenue(
                OrderStatus.PAID,
                start,
                end,
                PageRequest.of(0, cappedLimit));

        List<CategoryItem> items = new ArrayList<>();
        if (results.isEmpty()) {
            return new TopCategoryResponse(items);
        }

        BigDecimal maxRevenue = results.stream()
                .map(r -> r.totalRevenue() != null ? BigDecimal.valueOf(r.totalRevenue()) : BigDecimal.ZERO)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);

        if (maxRevenue.compareTo(BigDecimal.ZERO) == 0) {
            maxRevenue = BigDecimal.ONE;
        }

        int rank = 1;
        for (CategoryRevenueData data : results) {
            BigDecimal revenue = data.totalRevenue() != null
                    ? BigDecimal.valueOf(data.totalRevenue())
                    : BigDecimal.ZERO;

            int percent = revenue
                    .multiply(BigDecimal.valueOf(100))
                    .divide(maxRevenue, 0, RoundingMode.HALF_UP)
                    .intValue();

            items.add(new CategoryItem(
                    rank++,
                    data.categoryName() != null ? data.categoryName() : "Unknown",
                    CurrencyFormatter.format(revenue),
                    percent,
                    null));
        }

        return new TopCategoryResponse(items);
    }
}
