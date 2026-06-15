package com.app.palate.dashboard.topCategories;

import com.app.palate.order.OrderStatus;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopCategoryService {

    private final TopCategoryRepository topCategoryRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    public TopCategoryResponse getTopCategories(DashboardPeriod period, int limit) {
        // 1. Calculate explicit zone-aware boundaries
        ZonedDateTime nowZoned = ZonedDateTime.now(zoneId);
        ZonedDateTime startZoned = getStartOfPeriodZoned(period, nowZoned);

        Instant start = startZoned.toInstant();
        Instant end = nowZoned.toInstant();

        // Enforce hard cap limit to a maximum of 7 items to align with menu items
        int optimizedLimit = Math.min(limit, 5);

        // 2. Fetch data via the dedicated projection repository matching the optimized limit
        Pageable pageable = PageRequest.of(0, optimizedLimit);
        List<CategoryRevenueData> results = topCategoryRepository.findTopCategoriesByRevenue(
                OrderStatus.PAID,
                start,
                end,
                pageable
        );

        List<CategoryItem> items = new ArrayList<>();

        if (results.isEmpty()) {
            return new TopCategoryResponse(items);
        }

        // 3. Find max revenue safely using the new Double data type
        BigDecimal maxRevenue = results.stream()
                .map(r -> r.totalRevenue() != null ? BigDecimal.valueOf(r.totalRevenue()) : BigDecimal.ZERO)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);

        if (maxRevenue.compareTo(BigDecimal.ZERO) == 0) {
            maxRevenue = BigDecimal.ONE;
        }

        // 4. Map records safely into DTO objects
        int rank = 1;
        for (CategoryRevenueData data : results) {
            BigDecimal revenue = data.totalRevenue() != null ? BigDecimal.valueOf(data.totalRevenue()) : BigDecimal.ZERO;
            
            // Calculate percentage share relative to top category item 
            int percent = revenue
                    .multiply(BigDecimal.valueOf(100))
                    .divide(maxRevenue, 0, RoundingMode.HALF_UP)
                    .intValue();

            items.add(new CategoryItem(
                    rank++,
                    data.categoryName() != null ? data.categoryName() : "Unknown",
                    formatCurrency(revenue.longValue()),
                    percent,
                    null
            ));
        }

        return new TopCategoryResponse(items);
    }

    private ZonedDateTime getStartOfPeriodZoned(DashboardPeriod period, ZonedDateTime now) {
        return switch (period) {
            case TODAY -> now.toLocalDate().atStartOfDay(zoneId);
            case THIS_WEEK -> now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay(zoneId);
            case THIS_MONTH -> now.withDayOfMonth(1).toLocalDate().atStartOfDay(zoneId);
            case THIS_YEAR -> now.withDayOfYear(1).toLocalDate().atStartOfDay(zoneId);
        };
    }

    private String formatCurrency(long amount) {
        if (amount >= 1_000_000) return "₦" + (amount / 1_000_000) + "M";
        if (amount >= 1_000) return "₦" + (amount / 1_000) + "k";
        return "₦" + amount;
    }
}