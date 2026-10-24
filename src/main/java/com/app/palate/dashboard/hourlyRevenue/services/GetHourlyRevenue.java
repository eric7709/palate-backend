package com.app.palate.dashboard.hourlyRevenue.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.dashboard.hourlyRevenue.HourlyRevenueRepository;
import com.app.palate.dashboard.hourlyRevenue.dto.HourlyRevenueData;
import com.app.palate.dashboard.hourlyRevenue.dto.HourlyRevenueDTO;
import com.app.palate.dashboard.hourlyRevenue.dto.HourlyRevenueResponseDTO;
import com.app.palate.dashboard.utils.CurrencyFormatter;
import com.app.palate.dashboard.utils.DashboardPeriods;
import com.app.palate.order.enums.OrderStatus;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

/** Paid revenue for the period in two-hour buckets, with the change against the previous period. */
@Service
@RequiredArgsConstructor
public class GetHourlyRevenue {
    private static final int[][] BUCKETS = {
            { 0, 1 }, { 2, 3 }, { 4, 5 }, { 6, 7 }, { 8, 9 }, { 10, 11 },
            { 12, 13 }, { 14, 15 }, { 16, 17 }, { 18, 19 }, { 20, 21 }, { 22, 23 }
    };

    private final HourlyRevenueRepository hourlyRevenueRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public HourlyRevenueResponseDTO get(DashboardPeriod period) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime currentStartZoned = DashboardPeriods.startOf(period, now);
        ZonedDateTime previousStartZoned = DashboardPeriods.previousStartOf(period, currentStartZoned);

        Instant currentStart = currentStartZoned.toInstant();
        Instant currentEnd = now.toInstant();
        Instant previousStart = previousStartZoned.toInstant();

        // Current and previous orders come back from one query
        List<HourlyRevenueData> results = hourlyRevenueRepository.getCombinedHourlyRevenue(
                OrderStatus.PAID,
                previousStart,
                currentStart,
                currentEnd);

        Map<Integer, BigDecimal> currentByHour = new HashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal previousTotal = BigDecimal.ZERO;

        for (HourlyRevenueData r : results) {
            BigDecimal value = r.total() != null ? BigDecimal.valueOf(r.total()) : BigDecimal.ZERO;
            int hour = r.createdAt().atZone(zoneId).getHour();
            if ("CURRENT".equals(r.periodFlag())) {
                currentByHour.merge(hour, value, BigDecimal::add);
                totalRevenue = totalRevenue.add(value);
            } else if ("PREVIOUS".equals(r.periodFlag())) {
                previousTotal = previousTotal.add(value);
            }
        }

        List<HourlyRevenueDTO> dataset = new ArrayList<>();
        for (int[] bucket : BUCKETS) {
            BigDecimal bucketSum = BigDecimal.ZERO;
            for (int hour : bucket) {
                bucketSum = bucketSum.add(currentByHour.getOrDefault(hour, BigDecimal.ZERO));
            }

            dataset.add(new HourlyRevenueDTO(
                    formatHourLabel(bucket[0]),
                    bucketSum.longValue(),
                    CurrencyFormatter.format(bucketSum),
                    null));
        }

        return new HourlyRevenueResponseDTO(
                dataset,
                totalRevenue.longValue(),
                previousTotal.longValue(),
                changePercent(totalRevenue, previousTotal));
    }

    private double changePercent(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) > 0) {
            return current.subtract(previous)
                    .divide(previous, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
        return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
    }

    private String formatHourLabel(int hour) {
        if (hour == 0)
            return "12am";
        if (hour == 12)
            return "12pm";
        return hour > 12 ? (hour - 12) + "pm" : hour + "am";
    }
}
