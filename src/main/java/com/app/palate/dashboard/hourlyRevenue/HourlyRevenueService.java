package com.app.palate.dashboard.hourlyRevenue;

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
public class HourlyRevenueService {

    private final HourlyRevenueRepository hourlyRevenueRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    public HourlyRevenueResponseDTO getHourlyRevenue(DashboardPeriod period) {
        // 1. Establish clear explicit date boundaries matching corporate frameworks
        ZonedDateTime nowZoned = ZonedDateTime.now(zoneId);
        ZonedDateTime currentStartZoned = getStartOfPeriodZoned(period, nowZoned);
        ZonedDateTime previousStartZoned = getStartOfPreviousPeriodZoned(period, currentStartZoned);

        Instant currentStart = currentStartZoned.toInstant();
        Instant currentEnd = nowZoned.toInstant();
        Instant previousStart = previousStartZoned.toInstant();

        // 2. Query data in a single high-performance combined database execution
        List<HourlyRevenueData> results = hourlyRevenueRepository.getCombinedHourlyRevenue(
                OrderStatus.PAID,
                previousStart,
                currentStart,
                currentEnd);

        // 3. Map values safely into fast local lookup structures
        Map<Integer, BigDecimal> currentMap = new HashMap<>();
        Map<Integer, BigDecimal> previousMap = new HashMap<>();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal previousTotal = BigDecimal.ZERO;

        // Service — extract hour in local zone
        for (HourlyRevenueData r : results) {
            BigDecimal value = r.total() != null ? BigDecimal.valueOf(r.total()) : BigDecimal.ZERO;
            int hour = r.createdAt().atZone(zoneId).getHour();
            if ("CURRENT".equals(r.periodFlag())) {
                currentMap.merge(hour, value, BigDecimal::add);
                totalRevenue = totalRevenue.add(value);
            } else if ("PREVIOUS".equals(r.periodFlag())) {
                previousMap.merge(hour, value, BigDecimal::add);
                previousTotal = previousTotal.add(value);
            }
        }

        // 4. Populate analytical 2-hour interval visualization display buckets
        int[][] buckets = {
                { 0, 1 }, { 2, 3 }, { 4, 5 }, { 6, 7 }, { 8, 9 }, { 10, 11 },
                { 12, 13 }, { 14, 15 }, { 16, 17 }, { 18, 19 }, { 20, 21 }, { 22, 23 }
        };

        List<HourlyRevenueDTO> dataset = new ArrayList<>();
        for (int[] bucket : buckets) {
            BigDecimal currentBucketSum = BigDecimal.ZERO;
            for (int hour : bucket) {
                currentBucketSum = currentBucketSum.add(currentMap.getOrDefault(hour, BigDecimal.ZERO));
            }

            long bucketValue = currentBucketSum.longValue();
            String timeLabel = formatHourLabel(bucket[0]);

            dataset.add(new HourlyRevenueDTO(
                    timeLabel,
                    bucketValue,
                    formatCurrency(currentBucketSum),
                    null));
        }

        // 5. Compute contextual change metric summaries
        double changePercent = 0.0;
        if (previousTotal.compareTo(BigDecimal.ZERO) > 0) {
            changePercent = totalRevenue.subtract(previousTotal)
                    .divide(previousTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        } else if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            changePercent = 100.0;
        }

        return new HourlyRevenueResponseDTO(
                dataset,
                totalRevenue.longValue(),
                previousTotal.longValue(),
                changePercent);
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

    private String formatHourLabel(int hour) {
        if (hour == 0)
            return "12am";
        if (hour == 12)
            return "12pm";
        return hour > 12 ? (hour - 12) + "pm" : hour + "am";
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null)
            return "₦0";
        long value = amount.longValue();
        if (value >= 1_000_000)
            return "₦" + (value / 1_000_000) + "M";
        if (value >= 1_000)
            return "₦" + (value / 1_000) + "k";
        return "₦" + value;
    }
}