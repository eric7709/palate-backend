package com.app.palate.analytics.services;

import static com.app.palate.analytics.utils.RowValues.nullSafe;
import static com.app.palate.analytics.utils.RowValues.toDouble;
import static com.app.palate.analytics.utils.RowValues.toLong;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.analytics.AnalyticsRepository;
import com.app.palate.analytics.dto.AnalyticsSummaryByDTO;
import com.app.palate.analytics.utils.AnalyticsRowMapper;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/**
 * Builds the full analytics summary for a date range (both ends included, UTC): headline numbers,
 * top and least performers by sales and by order count, sales over time, and take-out vs dine-in.
 * Each ranked list is cut to the requested limit.
 */
@Service
@RequiredArgsConstructor
public class GetAnalyticsSummary {
    private static final int DEFAULT_LIMIT = 10;

    private final AnalyticsRepository repo;
    private final AnalyticsRowMapper rows;

    @Transactional(readOnly = true)
    public AnalyticsSummaryByDTO get(LocalDate from, LocalDate to, int limit) {
        ValidationUtils.requireNonNull(from, "From date");
        ValidationUtils.requireNonNull(to, "To date");
        if (from.isAfter(to)) {
            throw new BadRequestException("From date must not be after to date");
        }
        int max = limit > 0 ? limit : DEFAULT_LIMIT;

        Instant start = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        // ── KPIs ─────────────────────────────────────────────────────────────
        double totalRevenue = nullSafe(repo.totalRevenue(start, end));
        long totalOrders = nullSafe(repo.totalOrderCount(start, end));
        long cancelled = nullSafe(repo.cancelledOrderCount(start, end));
        long totalPlaced = nullSafe(repo.totalOrdersPlaced(start, end));
        double avgOrderValue = nullSafe(repo.averageOrderValue(start, end));
        double avgItems = nullSafe(repo.averageItemsPerOrder(start, end));
        double cancellationRate = totalPlaced > 0 ? (double) cancelled / totalPlaced * 100 : 0.0;

        // ── New vs returning ──────────────────────────────────────────────────
        List<Object[]> crRows = repo.newVsReturningCustomers(start, end);
        Object[] cr = crRows.isEmpty() ? new Object[] { 0L, 0L } : crRows.get(0);
        long newCustomers = toLong(cr[0]);
        long returning = toLong(cr[1]);

        // ── Take-out vs dine-in ───────────────────────────────────────────────
        long takeOutCount = 0, dineInCount = 0;
        double takeOutRevenue = 0, dineInRevenue = 0;
        for (Object[] r : repo.takeOutVsDineIn(start, end)) {
            boolean isTakeOut = (Boolean) r[0];
            if (isTakeOut) {
                takeOutCount = toLong(r[1]);
                takeOutRevenue = toDouble(r[2]);
            } else {
                dineInCount = toLong(r[1]);
                dineInRevenue = toDouble(r[2]);
            }
        }

        return AnalyticsSummaryByDTO.builder()
                // KPIs
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(avgOrderValue)
                .averageItemsPerOrder(avgItems)
                .cancelledOrders(cancelled)
                .cancellationRate(cancellationRate)
                .newCustomers(newCustomers)
                .returningCustomers(returning)

                // Top by sales
                .topWaitersBySales(cap(rows.toAccountSales(repo.topWaitersBySales(start, end)), max))
                .topCashiersBySales(cap(rows.toAccountSales(repo.topCashiersBySales(start, end)), max))
                .topTablesBySales(cap(rows.toTableSales(repo.topTablesBySales(start, end)), max))
                .topRoomsBySales(cap(rows.toRoomSales(repo.topRoomsBySales(start, end)), max))
                .topCategoriesBySales(cap(rows.toCategorySales(repo.topCategoriesBySales(start, end)), max))
                .topMenuItemsBySales(cap(rows.toMenuItemSales(repo.topMenuItemsBySales(start, end)), max))
                .topCustomersBySales(cap(rows.toCustomerSales(repo.topCustomersBySales(start, end)), max))

                // Top by count
                .topWaitersByCount(cap(rows.toAccountSales(repo.topWaitersByCount(start, end)), max))
                .topCashiersByCount(cap(rows.toAccountSales(repo.topCashiersByCount(start, end)), max))
                .topTablesByCount(cap(rows.toTableSales(repo.topTablesByCount(start, end)), max))
                .topRoomsByCount(cap(rows.toRoomSales(repo.topRoomsByCount(start, end)), max))
                .topCategoriesByCount(cap(rows.toCategorySales(repo.topCategoriesByCount(start, end)), max))
                .topMenuItemsByCount(cap(rows.toMenuItemSales(repo.topMenuItemsByCount(start, end)), max))
                .topCustomersByCount(cap(rows.toCustomerSales(repo.topCustomersByCount(start, end)), max))

                // Least by sales
                .leastWaitersBySales(cap(rows.toAccountSales(repo.leastWaitersBySales(start, end)), max))
                .leastCashiersBySales(cap(rows.toAccountSales(repo.leastCashiersBySales(start, end)), max))
                .leastTablesBySales(cap(rows.toTableSales(repo.leastTablesBySales(start, end)), max))
                .leastRoomsBySales(cap(rows.toRoomSales(repo.leastRoomsBySales(start, end)), max))
                .leastCategoriesBySales(cap(rows.toCategorySales(repo.leastCategoriesBySales(start, end)), max))
                .leastMenuItemsBySales(cap(rows.toMenuItemSales(repo.leastMenuItemsBySales(start, end)), max))

                // Least by count
                .leastWaitersByCount(cap(rows.toAccountSales(repo.leastWaitersByCount(start, end)), max))
                .leastCashiersByCount(cap(rows.toAccountSales(repo.leastCashiersByCount(start, end)), max))
                .leastTablesByCount(cap(rows.toTableSales(repo.leastTablesByCount(start, end)), max))
                .leastRoomsByCount(cap(rows.toRoomSales(repo.leastRoomsByCount(start, end)), max))
                .leastCategoriesByCount(cap(rows.toCategorySales(repo.leastCategoriesByCount(start, end)), max))
                .leastMenuItemsByCount(cap(rows.toMenuItemSales(repo.leastMenuItemsByCount(start, end)), max))

                // Time-series
                .salesByDay(rows.toDaySales(repo.salesByDayOfWeek(start, end)))
                .salesByHour(rows.toHourSales(repo.salesByHour(start, end)))
                .revenueOverTime(rows.toDailyRevenue(repo.revenueOverTime(start, end)))

                // Order composition
                .takeOutCount(takeOutCount)
                .takeOutRevenue(takeOutRevenue)
                .dineInCount(dineInCount)
                .dineInRevenue(dineInRevenue)
                .build();
    }

    private <T> List<T> cap(List<T> list, int limit) {
        return list.size() <= limit ? list : list.subList(0, limit);
    }
}
