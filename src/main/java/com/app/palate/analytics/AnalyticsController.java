package com.app.palate.analytics;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.app.palate.analytics.dto.AnalyticsRequestDTO;
import com.app.palate.analytics.dto.AnalyticsSummaryByDTO;
import com.app.palate.analytics.services.GetAnalyticsSummary;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/palate/analytics")
public class AnalyticsController {
    private final GetAnalyticsSummary getAnalyticsSummary;

    /** GET /api/palate/analytics/summary?from=2024-01-01&to=2024-12-31&limit=10 */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    public AnalyticsSummaryByDTO getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {
        return getAnalyticsSummary.get(from, to, limit);
    }

    /** POST /api/palate/analytics/summary with body { "from": "2024-01-01", "to": "2024-12-31", "limit": 10 } */
    @PostMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    public AnalyticsSummaryByDTO getSummaryPost(@Valid @RequestBody AnalyticsRequestDTO request) {
        return getAnalyticsSummary.get(request.getFrom(), request.getTo(), request.getLimit());
    }
}
