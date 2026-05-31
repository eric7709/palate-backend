package com.app.palate.analytics;

import com.app.palate.analytics.dto.*;
import com.app.palate.analytics.projections.CancelledItemProjection;
import com.app.palate.analytics.projections.PeakHourProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/palate/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ── Revenue ──────────────────────────────────────────────────────────────

    @GetMapping("/revenue/summary")
    public ResponseEntity<RevenueSummaryDTO> getRevenueSummary(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        return ResponseEntity.ok(analyticsService.getRevenueSummary(startDate, endDate));
    }

    @GetMapping("/revenue/over-time")
    public ResponseEntity<List<RevenueOverTimeDTO>> getRevenueOverTime(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        return ResponseEntity.ok(analyticsService.getRevenueOverTime(startDate, endDate));
    }

    // ── Orders ───────────────────────────────────────────────────────────────

    @GetMapping("/orders/by-status")
    public ResponseEntity<List<OrdersByStatusDTO>> getOrdersByStatus(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        return ResponseEntity.ok(analyticsService.getOrdersByStatus(startDate, endDate));
    }

    @GetMapping("/orders/peak-hours")
    public ResponseEntity<List<PeakHourProjection>> getPeakHours(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        return ResponseEntity.ok(analyticsService.getPeakHours(startDate, endDate));
    }

    // ── Menu ─────────────────────────────────────────────────────────────────

    @GetMapping("/menu/top-items")
    public ResponseEntity<List<MenuItemPerformanceDTO>> getTopItems(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopItems(startDate, endDate, limit));
    }

    @GetMapping("/menu/least-items")
    public ResponseEntity<List<MenuItemPerformanceDTO>> getLeastItems(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getLeastItems(startDate, endDate, limit));
    }

    @GetMapping("/menu/cancelled-items")
    public ResponseEntity<List<CancelledItemProjection>> getMostCancelledItems(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getMostCancelledItems(startDate, endDate, limit));
    }

    // ── Customers ────────────────────────────────────────────────────────────

    @GetMapping("/customers/summary")
    public ResponseEntity<CustomerSummaryDTO> getCustomerSummary(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        return ResponseEntity.ok(analyticsService.getCustomerSummary(startDate, endDate));
    }

    @GetMapping("/customers/top")
    public ResponseEntity<List<TopCustomerDTO>> getTopCustomers(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopCustomers(startDate, endDate, limit));
    }

    // ── Staff ────────────────────────────────────────────────────────────────

    @GetMapping("/staff/waiters")
    public ResponseEntity<List<StaffPerformanceDTO>> getOrdersPerWaiter(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        return ResponseEntity.ok(analyticsService.getOrdersPerWaiter(startDate, endDate));
    }

    @GetMapping("/staff/cashiers")
    public ResponseEntity<List<StaffPerformanceDTO>> getRevenuePerCashier(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        return ResponseEntity.ok(analyticsService.getRevenuePerCashier(startDate, endDate));
    }

    // ── Tables ───────────────────────────────────────────────────────────────

    @GetMapping("/tables/activity")
    public ResponseEntity<List<TableActivityDTO>> getTableActivity(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        return ResponseEntity.ok(analyticsService.getTableActivity(startDate, endDate));
    }
}