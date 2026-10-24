package com.app.palate.dashboard.quickStats.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.dashboard.quickStats.QuickStatsRepository;
import com.app.palate.dashboard.quickStats.dto.QuickStatsData;
import com.app.palate.dashboard.quickStats.dto.QuickStatsResponse;
import com.app.palate.dashboard.utils.CurrencyFormatter;
import com.app.palate.dashboard.utils.DashboardPeriods;
import com.app.palate.order.enums.OrderStatus;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

/** Revenue, order count, average order value and repeat-customer rate for the period (completed and paid orders). */
@Service
@RequiredArgsConstructor
public class GetQuickStats {
    private final QuickStatsRepository quickStatsRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    @Transactional(readOnly = true)
    public QuickStatsResponse get(DashboardPeriod period) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        Instant start = DashboardPeriods.startOf(period, now).toInstant();
        Instant end = now.toInstant();

        QuickStatsData metrics = quickStatsRepository.getQuickStatsMetrics(
                List.of(OrderStatus.COMPLETED, OrderStatus.PAID),
                start,
                end).orElse(new QuickStatsData(0L, 0.0, 0L, 0L));

        long totalOrders = metrics.totalOrders();
        BigDecimal revenue = metrics.totalRevenue() != null
                ? BigDecimal.valueOf(metrics.totalRevenue())
                : BigDecimal.ZERO;

        long averageOrderValue = 0;
        if (totalOrders > 0) {
            averageOrderValue = revenue
                    .divide(BigDecimal.valueOf(totalOrders), 0, RoundingMode.HALF_UP)
                    .longValue();
        }

        long retentionPercent = 0;
        if (metrics.totalCustomers() > 0) {
            retentionPercent = Math.round((double) metrics.repeatCustomers() / metrics.totalCustomers() * 100);
        }

        String subLabel = periodLabel(period);

        return new QuickStatsResponse(List.of(
                new QuickStatsResponse.StatItem("Total Revenue", CurrencyFormatter.format(revenue), subLabel),
                new QuickStatsResponse.StatItem("Total Orders", String.valueOf(totalOrders), subLabel),
                new QuickStatsResponse.StatItem("Average Order Value", CurrencyFormatter.format(averageOrderValue), subLabel),
                new QuickStatsResponse.StatItem("Repeat Customer Rate", retentionPercent + "%", subLabel)));
    }

    private String periodLabel(DashboardPeriod period) {
        return switch (period) {
            case TODAY -> "vs yesterday snapshot";
            case THIS_WEEK -> "vs last week snapshot";
            case THIS_MONTH -> "vs last month snapshot";
            case THIS_YEAR -> "vs last year snapshot";
        };
    }
}
