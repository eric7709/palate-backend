package com.app.palate.dashboard.quickStats;

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
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuickStatsService {

    private final QuickStatsRepository quickStatsRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    public QuickStatsResponse getQuickStats(DashboardPeriod period) {
        // 1. Calculate explicit Zoned Date Boundaries
        ZonedDateTime nowZoned = ZonedDateTime.now(zoneId);
        ZonedDateTime startZoned = getStartOfPeriodZoned(period, nowZoned);
        
        Instant start = startZoned.toInstant();
        Instant end = nowZoned.toInstant();

        // 2. Fetch aggregated structural query metrics
        QuickStatsData metrics = quickStatsRepository.getQuickStatsMetrics(
                List.of(OrderStatus.COMPLETED, OrderStatus.PAID),
                start,
                end
        ).orElse(new QuickStatsData(0L, 0.0, 0L, 0L));

        // 3. Extract data values safely falling back on defaults
        long totalOrders = metrics.totalOrders();
        
        // Map the new Double field type safely to a BigDecimal instance
        BigDecimal revenueVal = metrics.totalRevenue() != null ? BigDecimal.valueOf(metrics.totalRevenue()) : BigDecimal.ZERO;
        long totalCustomers = metrics.totalCustomers();
        long repeatCustomers = metrics.repeatCustomers();

        // 4. Run structural dashboard arithmetic computations
        long averageOrderValue = 0;
        if (totalOrders > 0) {
            averageOrderValue = revenueVal
                    .divide(BigDecimal.valueOf(totalOrders), 0, RoundingMode.HALF_UP)
                    .longValue();
        }

        long retentionPercent = 0;
        if (totalCustomers > 0) {
            retentionPercent = Math.round((double) repeatCustomers / totalCustomers * 100);
        }

        String subLabel = periodLabel(period);

        // 5. Construct mapped display models
        List<QuickStatsResponse.StatItem> items = List.of(
                new QuickStatsResponse.StatItem("Total Revenue", formatCurrency(revenueVal.longValue()), subLabel),
                new QuickStatsResponse.StatItem("Total Orders", String.valueOf(totalOrders), subLabel),
                new QuickStatsResponse.StatItem("Average Order Value", formatCurrency(averageOrderValue), subLabel),
                new QuickStatsResponse.StatItem("Repeat Customer Rate", retentionPercent + "%", subLabel)
        );

        return new QuickStatsResponse(items);
    }

    private ZonedDateTime getStartOfPeriodZoned(DashboardPeriod period, ZonedDateTime now) {
        return switch (period) {
            case TODAY -> now.toLocalDate().atStartOfDay(zoneId);
            case THIS_WEEK -> now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay(zoneId);
            case THIS_MONTH -> now.withDayOfMonth(1).toLocalDate().atStartOfDay(zoneId);
            case THIS_YEAR -> now.withDayOfYear(1).toLocalDate().atStartOfDay(zoneId);
        };
    }

    private String periodLabel(DashboardPeriod period) {
        return switch (period) {
            case TODAY -> "vs yesterday snapshot";
            case THIS_WEEK -> "vs last week snapshot";
            case THIS_MONTH -> "vs last month snapshot";
            case THIS_YEAR -> "vs last year snapshot";
        };
    }

    private String formatCurrency(long amount) {
        if (amount >= 1_000_000) return "₦" + (amount / 1_000_000) + "M";
        if (amount >= 1_000) return "₦" + (amount / 1_000) + "k";
        return "₦" + amount;
    }
}