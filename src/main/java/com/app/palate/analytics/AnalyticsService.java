package com.app.palate.analytics;

import com.app.palate.analytics.dto.*;
import com.app.palate.analytics.projections.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository repo;

    // ── Revenue ──────────────────────────────────────────────────────────────

    public RevenueSummaryDTO getRevenueSummary(Instant start, Instant end) {
        RevenueSummaryProjection p = repo.getRevenueSummary(start, end);
        return RevenueSummaryDTO.builder()
                .totalRevenue(p.getTotalRevenue())
                .avgOrderValue(p.getAvgOrderValue())
                .totalOrders(p.getTotalOrders())
                .build();
    }

    public List<RevenueOverTimeDTO> getRevenueOverTime(Instant start, Instant end) {
        String granularity = resolveGranularity(start, end);
        return repo.getRevenueOverTime(granularity, start, end).stream()
                .map(p -> RevenueOverTimeDTO.builder()
                        .period(p.getPeriod())
                        .revenue(p.getRevenue())
                        .orderCount(p.getOrderCount())
                        .build())
                .toList();
    }

    // ── Orders ───────────────────────────────────────────────────────────────

    public List<OrdersByStatusDTO> getOrdersByStatus(Instant start, Instant end) {
        return repo.countByStatus(start, end).stream()
                .map(p -> OrdersByStatusDTO.builder()
                        .status(p.getStatus())
                        .count(p.getCount())
                        .build())
                .toList();
    }

    public List<PeakHourProjection> getPeakHours(Instant start, Instant end) {
        return repo.getPeakHours(start, end);
    }

    // ── Menu ─────────────────────────────────────────────────────────────────

    public List<MenuItemPerformanceDTO> getTopItems(Instant start, Instant end, int limit) {
        return repo.getTopItems(start, end, PageRequest.of(0, limit)).stream()
                .map(p -> MenuItemPerformanceDTO.builder()
                        .itemId(p.getItemId())
                        .itemName(p.getItemName())
                        .totalQuantity(p.getTotalQuantity())
                        .totalRevenue(p.getTotalRevenue())
                        .build())
                .toList();
    }

    public List<MenuItemPerformanceDTO> getLeastItems(Instant start, Instant end, int limit) {
        return repo.getTopItems(start, end, PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .sorted(Comparator.comparingLong(MenuItemPerformanceProjection::getTotalQuantity))
                .limit(limit)
                .map(p -> MenuItemPerformanceDTO.builder()
                        .itemId(p.getItemId())
                        .itemName(p.getItemName())
                        .totalQuantity(p.getTotalQuantity())
                        .totalRevenue(p.getTotalRevenue())
                        .build())
                .toList();
    }

    public List<CancelledItemProjection> getMostCancelledItems(Instant start, Instant end, int limit) {
        return repo.getMostCancelledItems(start, end, PageRequest.of(0, limit));
    }

    // ── Customers ────────────────────────────────────────────────────────────

    public CustomerSummaryDTO getCustomerSummary(Instant start, Instant end) {
        CustomerSummaryProjection p = repo.getCustomerSummary(start, end);
        return CustomerSummaryDTO.builder()
                .newCustomers(p.getNewCustomers())
                .returningCustomers(p.getReturningCustomers())
                .build();
    }

    public List<TopCustomerDTO> getTopCustomers(Instant start, Instant end, int limit) {
        return repo.getTopCustomers(start, end, PageRequest.of(0, limit)).stream()
                .map(p -> TopCustomerDTO.builder()
                        .customerId(p.getCustomerId())
                        .customerName(p.getCustomerName())
                        .orderCount(p.getOrderCount())
                        .totalSpent(p.getTotalSpent())
                        .build())
                .toList();
    }

    // ── Staff ────────────────────────────────────────────────────────────────

    public List<StaffPerformanceDTO> getOrdersPerWaiter(Instant start, Instant end) {
        return repo.getOrdersPerWaiter(start, end).stream()
                .map(p -> StaffPerformanceDTO.builder()
                        .staffId(p.getStaffId())
                        .staffName(p.getStaffName())
                        .orderCount(p.getOrderCount())
                        .totalValue(p.getTotalValue())
                        .build())
                .toList();
    }

    public List<StaffPerformanceDTO> getRevenuePerCashier(Instant start, Instant end) {
        return repo.getRevenuePerCashier(start, end).stream()
                .map(p -> StaffPerformanceDTO.builder()
                        .staffId(p.getStaffId())
                        .staffName(p.getStaffName())
                        .orderCount(p.getOrderCount())
                        .totalValue(p.getTotalValue())
                        .build())
                .toList();
    }

    // ── Tables ───────────────────────────────────────────────────────────────

    public List<TableActivityDTO> getTableActivity(Instant start, Instant end) {
        return repo.getTableActivity(start, end).stream()
                .map(p -> TableActivityDTO.builder()
                        .tableId(p.getTableId())
                        .tableName(p.getTableName())
                        .tableNumber(p.getTableNumber())
                        .orderCount(p.getOrderCount())
                        .totalRevenue(p.getTotalRevenue())
                        .build())
                .toList();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String resolveGranularity(Instant start, Instant end) {
        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 1)  return "hour";
        if (days <= 31) return "day";
        if (days <= 90) return "week";
        return "month";
    }
}