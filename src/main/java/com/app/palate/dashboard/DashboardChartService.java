package com.app.palate.dashboard;

import com.app.palate.dashboard.DashboardChartStats.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardChartService {

    private final DashboardChartRepository chartRepository;

    public DashboardChartStats getChartStats(Instant from, Instant to) {
        return new DashboardChartStats(
            buildSalesOverview(from, to),
            buildRevenueByHour(from, to)
        );
    }

    private List<DailyRevenue> buildSalesOverview(Instant from, Instant to) {
        return chartRepository.findDailyRevenue(from, to)
            .stream()
            .map(p -> new DailyRevenue(
                p.getDay().toString(),      // "2024-11-01"
                nullSafe(p.getRevenue()),
                nullSafe(p.getOrderCount())
            ))
            .toList();
    }

    private List<HourlyRevenue> buildRevenueByHour(Instant from, Instant to) {
        return chartRepository.findHourlyRevenue(from, to)
            .stream()
            .map(p -> new HourlyRevenue(
                p.getHour(),
                nullSafe(p.getRevenue()),
                nullSafe(p.getOrderCount())
            ))
            .toList();
    }

    private double nullSafe(Double val) {
        return val != null ? val : 0.0;
    }

    private long nullSafe(Long val) {
        return val != null ? val : 0L;
    }
}