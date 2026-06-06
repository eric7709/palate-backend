package com.app.palate.dashboard;

import com.app.palate.dashboard.DashboardTopStats.*;
import com.app.palate.dashboard.DashboardChartStats.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository repository;

    private static final ZoneId WAT = ZoneId.of("Africa/Lagos"); // UTC+1

    // ── Public API ────────────────────────────────────────────────────────────

    public DashboardDTO getDashboardData() {
        Instant now         = Instant.now();
        Instant startOfDay  = LocalDate.now(WAT).atStartOfDay(WAT).toInstant();
        Instant endOfDay    = startOfDay.plus(1, ChronoUnit.DAYS);
        Instant start30Days = now.minus(30, ChronoUnit.DAYS);

        return new DashboardDTO(
                getPaidOrdersVolumeByHour(startOfDay, endOfDay),
                getAvgOrderValueByTable(start30Days, now),
                repository.getTotalOrderVolume(startOfDay, endOfDay),
                getPeakHourToday(startOfDay, endOfDay)
        );
    }

    public DashboardChartStats getChartStats(Instant from, Instant to) {
        return new DashboardChartStats(
                buildSalesOverview(from, to),
                buildRevenueByHour(from, to)
        );
    }

    public DashboardTopStats getTopStats(Instant from, Instant to, int limit) {
        Duration period  = Duration.between(from, to);
        Instant prevFrom = from.minus(period);
        Instant prevTo   = from;
        var     page     = PageRequest.of(0, limit);

        return new DashboardTopStats(
                buildTopTables(from, to, prevFrom, prevTo, page),
                buildTopCategories(from, to, prevFrom, prevTo, page),
                buildTopItems(from, to, prevFrom, prevTo, page),
                buildTopWaiters(from, to, prevFrom, prevTo, page)
        );
    }

    // ── Dashboard helpers ─────────────────────────────────────────────────────

    private List<OrderHourDTO> getPaidOrdersVolumeByHour(Instant start, Instant end) {
        List<Object[]> results = repository.getPaidOrdersVolumeByHour(start, end);

        List<OrderHourDTO> hourly = new ArrayList<>();
        for (int i = 0; i < 24; i++)
            hourly.add(new OrderHourDTO(String.format("%02d:00", i), 0L));

        for (Object[] row : results) {
            int  localHour = utcHourToLocal(((Number) row[0]).intValue());
            long count     = ((Number) row[1]).longValue();
            hourly.set(localHour, new OrderHourDTO(String.format("%02d:00", localHour), count));
        }

        return hourly;
    }

    private List<TableAvgDTO> getAvgOrderValueByTable(Instant start, Instant end) {
        return repository.getAvgOrderValueByTable(start, end).stream()
                .map(row -> new TableAvgDTO((String) row[0], ((Number) row[1]).doubleValue()))
                .collect(Collectors.toList());
    }

    private PeakHourDTO getPeakHourToday(Instant start, Instant end) {
        List<Object[]> results = repository.getPeakOrderHour(start, end);
        if (results.isEmpty()) return new PeakHourDTO("N/A", 0L);

        Object[] top  = results.get(0);
        int  localHour = utcHourToLocal(((Number) top[0]).intValue());
        long count     = ((Number) top[1]).longValue();

        return new PeakHourDTO(String.format("%02d:00", localHour), count);
    }

    // ── Chart helpers ─────────────────────────────────────────────────────────

    private List<DailyRevenue> buildSalesOverview(Instant from, Instant to) {
        return repository.findDailyRevenue(from, to).stream()
                .map(p -> new DailyRevenue(
                        p.getDay().toString(),
                        nullSafe(p.getRevenue()),
                        nullSafe(p.getOrderCount())))
                .toList();
    }

    private List<HourlyRevenue> buildRevenueByHour(Instant from, Instant to) {
        return repository.findHourlyRevenue(from, to).stream()
                .map(p -> new HourlyRevenue(
                        p.getHour(),
                        nullSafe(p.getRevenue()),
                        nullSafe(p.getOrderCount())))
                .toList();
    }

    // ── Top stats helpers ─────────────────────────────────────────────────────

    private List<TopTable> buildTopTables(Instant from, Instant to, Instant prevFrom, Instant prevTo, PageRequest page) {
        List<Object[]> curr = repository.findTopTables(from, to, page);
        List<Object[]> prev = repository.findTopTables(prevFrom, prevTo, page);
        Map<Long, Double> prevMap = toRevenueMap(prev, 3);
        double total = sumRevenue(curr, 3);

        return curr.stream().map(row -> {
            Long   id      = toLongId(row[0]);
            double revenue = toDouble(row[3]);
            return new TopTable(
                    id, (String) row[1], ((Number) row[2]).intValue(),
                    revenue, toLong(row[4]),
                    shareOf(revenue, total),
                    growth(prevMap.getOrDefault(id, 0.0), revenue));
        }).toList();
    }

    private List<TopCategory> buildTopCategories(Instant from, Instant to, Instant prevFrom, Instant prevTo, PageRequest page) {
        List<Object[]> curr = repository.findTopCategories(from, to, page);
        List<Object[]> prev = repository.findTopCategories(prevFrom, prevTo, page);
        Map<Long, Double> prevMap = toRevenueMap(prev, 2);
        double total = sumRevenue(curr, 2);

        return curr.stream().map(row -> {
            Long   id      = toLongId(row[0]);
            double revenue = toDouble(row[2]);
            return new TopCategory(
                    id, (String) row[1],
                    revenue, toLong(row[3]),
                    shareOf(revenue, total),
                    growth(prevMap.getOrDefault(id, 0.0), revenue));
        }).toList();
    }

    private List<TopItem> buildTopItems(Instant from, Instant to, Instant prevFrom, Instant prevTo, PageRequest page) {
        List<Object[]> curr = repository.findTopItems(from, to, page);
        List<Object[]> prev = repository.findTopItems(prevFrom, prevTo, page);
        Map<Long, Double> prevMap = toRevenueMap(prev, 3);
        double total = sumRevenue(curr, 3);

        return curr.stream().map(row -> {
            Long   id      = toLongId(row[0]);
            double revenue = toDouble(row[3]);
            return new TopItem(
                    id, (String) row[1], (String) row[2],
                    revenue, toLong(row[4]),
                    shareOf(revenue, total),
                    growth(prevMap.getOrDefault(id, 0.0), revenue));
        }).toList();
    }

    private List<TopWaiter> buildTopWaiters(Instant from, Instant to, Instant prevFrom, Instant prevTo, PageRequest page) {
        List<Object[]> curr = repository.findTopWaiters(from, to, page);
        List<Object[]> prev = repository.findTopWaiters(prevFrom, prevTo, page);
        Map<Long, Double> prevMap = toRevenueMap(prev, 2);
        double total = sumRevenue(curr, 2);

        return curr.stream().map(row -> {
            Long   id      = toLongId(row[0]);
            double revenue = toDouble(row[2]);
            return new TopWaiter(
                    id, (String) row[1],
                    revenue, toLong(row[3]),
                    shareOf(revenue, total),
                    growth(prevMap.getOrDefault(id, 0.0), revenue));
        }).toList();
    }

    // ── Shared utils ──────────────────────────────────────────────────────────

    private int utcHourToLocal(int utcHour) {
        return LocalDate.now(WAT)
                .atTime(utcHour, 0)
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(WAT)
                .getHour();
    }

    private Map<Long, Double> toRevenueMap(List<Object[]> rows, int revenueIndex) {
        return rows.stream().collect(Collectors.toMap(
                row -> toLongId(row[0]),
                row -> toDouble(row[revenueIndex]),
                (a, b) -> a));
    }

    private double sumRevenue(List<Object[]> rows, int index) {
        return rows.stream().mapToDouble(r -> toDouble(r[index])).sum();
    }

    private double shareOf(double revenue, double total) {
        return total == 0 ? 0 : (revenue / total) * 100.0;
    }

    private double growth(double prev, double curr) {
        return prev == 0 ? 0 : ((curr - prev) / prev) * 100.0;
    }

    private double toDouble(Object val) { return val instanceof Number n ? n.doubleValue() : 0.0; }
    private long   toLong(Object val)   { return val instanceof Number n ? n.longValue()   : 0L;  }
    private Long   toLongId(Object val) { return val instanceof Number n ? n.longValue()   : null; }
    private double nullSafe(Double val) { return val != null ? val : 0.0; }
    private long   nullSafe(Long val)   { return val != null ? val : 0L;  }
}