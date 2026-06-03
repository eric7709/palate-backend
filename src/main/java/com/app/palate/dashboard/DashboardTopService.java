package com.app.palate.dashboard;

import com.app.palate.dashboard.DashboardTopStats.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardTopService {

    private final DashboardTopRepository topRepository;

    public DashboardTopStats getTopStats(Instant from, Instant to, int limit) {
        Duration period   = Duration.between(from, to);
        Instant  prevFrom = from.minus(period);
        Instant  prevTo   = from;

        return new DashboardTopStats(
            buildTopTables(from, to, prevFrom, prevTo, limit),
            buildTopCategories(from, to, prevFrom, prevTo, limit),
            buildTopItems(from, to, prevFrom, prevTo, limit),
            buildTopWaiters(from, to, prevFrom, prevTo, limit)
        );
    }

    // ── Tables ────────────────────────────────────────────────────────────

    private List<TopTable> buildTopTables(Instant from, Instant to, Instant prevFrom, Instant prevTo, int limit) {
        List<Object[]> curr = topRepository.findTopTables(from, to, limit);
        List<Object[]> prev = topRepository.findTopTables(prevFrom, prevTo, limit);

        Map<Long, Double> prevRevenueById = toRevenueMap(prev, 3);

        double totalRevenue = curr.stream()
            .mapToDouble(row -> toDouble(row[3]))
            .sum();

        return curr.stream().map(row -> {
            Long   id           = toLongId(row[0]);
            double revenue      = toDouble(row[3]);
            long   orderCount   = toLong(row[4]);
            double sharePercent = totalRevenue == 0 ? 0 : (revenue / totalRevenue) * 100.0;
            double prevRev      = prevRevenueById.getOrDefault(id, 0.0);

            return new TopTable(
                id,
                (String) row[1],
                ((Number) row[2]).intValue(),
                revenue,
                orderCount,
                sharePercent,
                growth(prevRev, revenue)
            );
        }).toList();
    }

    // ── Categories ────────────────────────────────────────────────────────

    private List<TopCategory> buildTopCategories(Instant from, Instant to, Instant prevFrom, Instant prevTo, int limit) {
        List<Object[]> curr = topRepository.findTopCategories(from, to, limit);
        List<Object[]> prev = topRepository.findTopCategories(prevFrom, prevTo, limit);

        Map<Long, Double> prevRevenueById = toRevenueMap(prev, 2);

        double totalRevenue = curr.stream()
            .mapToDouble(row -> toDouble(row[2]))
            .sum();

        return curr.stream().map(row -> {
            Long   id           = toLongId(row[0]);
            double revenue      = toDouble(row[2]);
            double sharePercent = totalRevenue == 0 ? 0 : (revenue / totalRevenue) * 100.0;
            double prevRev      = prevRevenueById.getOrDefault(id, 0.0);

            return new TopCategory(
                id,
                (String) row[1],
                revenue,
                toLong(row[3]),
                sharePercent,
                growth(prevRev, revenue)
            );
        }).toList();
    }

    // ── Items ─────────────────────────────────────────────────────────────

    private List<TopItem> buildTopItems(Instant from, Instant to, Instant prevFrom, Instant prevTo, int limit) {
        List<Object[]> curr = topRepository.findTopItems(from, to, limit);
        List<Object[]> prev = topRepository.findTopItems(prevFrom, prevTo, limit);

        Map<Long, Double> prevRevenueById = toRevenueMap(prev, 3);

        double totalRevenue = curr.stream()
            .mapToDouble(row -> toDouble(row[3]))
            .sum();

        return curr.stream().map(row -> {
            Long   id           = toLongId(row[0]);
            double revenue      = toDouble(row[3]);
            double sharePercent = totalRevenue == 0 ? 0 : (revenue / totalRevenue) * 100.0;
            double prevRev      = prevRevenueById.getOrDefault(id, 0.0);

            return new TopItem(
                id,
                (String) row[1],
                (String) row[2],
                revenue,
                toLong(row[4]),
                sharePercent,
                growth(prevRev, revenue)
            );
        }).toList();
    }

    // ── Waiters ───────────────────────────────────────────────────────────

    private List<TopWaiter> buildTopWaiters(Instant from, Instant to, Instant prevFrom, Instant prevTo, int limit) {
        List<Object[]> curr = topRepository.findTopWaiters(from, to, limit);
        List<Object[]> prev = topRepository.findTopWaiters(prevFrom, prevTo, limit);

        Map<Long, Double> prevRevenueById = toRevenueMap(prev, 2);

        double totalRevenue = curr.stream()
            .mapToDouble(row -> toDouble(row[2]))
            .sum();

        return curr.stream().map(row -> {
            Long   id           = toLongId(row[0]);
            double revenue      = toDouble(row[2]);
            double sharePercent = totalRevenue == 0 ? 0 : (revenue / totalRevenue) * 100.0;
            double prevRev      = prevRevenueById.getOrDefault(id, 0.0);

            return new TopWaiter(
                id,
                (String) row[1],
                revenue,
                toLong(row[3]),
                sharePercent,
                growth(prevRev, revenue)
            );
        }).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Map<Long, Double> toRevenueMap(List<Object[]> rows, int revenueIndex) {
        return rows.stream().collect(Collectors.toMap(
            row -> toLongId(row[0]),
            row -> toDouble(row[revenueIndex]),
            (a, b) -> a
        ));
    }

    private double growth(double prev, double curr) {
        if (prev == 0) return 0;
        return ((curr - prev) / prev) * 100.0;
    }

    private double toDouble(Object val) {
        return val instanceof Number n ? n.doubleValue() : 0.0;
    }

    private long toLong(Object val) {
        return val instanceof Number n ? n.longValue() : 0L;
    }

    private Long toLongId(Object val) {
        return val instanceof Number n ? n.longValue() : null;
    }
}