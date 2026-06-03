package com.app.palate.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/palate/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardChartService dashboardChartService;
    private final DashboardTopService dashboardTopService;

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public DashboardDTO getDashboardData() {
        return dashboardService.getDashboardData();
    }

    @GetMapping("/top")
    public ResponseEntity<DashboardTopStats> getTopStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit) {

        if (from.isAfter(to))
            return ResponseEntity.badRequest().build();

        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        return ResponseEntity.ok(dashboardTopService.getTopStats(fromInstant, toInstant, limit));
    }

    @GetMapping("/charts")
    public ResponseEntity<DashboardChartStats> getChartStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (from.isAfter(to))
            return ResponseEntity.badRequest().build();

        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        return ResponseEntity.ok(dashboardChartService.getChartStats(fromInstant, toInstant));
    }
}