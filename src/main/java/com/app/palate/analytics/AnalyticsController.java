package com.app.palate.analytics;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/palate/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    /**
     * GET /api/v1/analytics/summary?from=2024-01-01&to=2024-12-31&limit=10
     *
     * Returns the full analytics summary for the given date range.
     * Restrict to ADMIN / MANAGER roles as appropriate.
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AnalyticsSummaryDTO> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.getSummary(from, to, limit));
    }

    /**
     * POST /api/v1/analytics/summary  (body: { "from": "2024-01-01", "to": "2024-12-31", "limit": 10 })
     *
     * Alternative POST endpoint — useful if you prefer passing the range in the body.
     */
    @PostMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AnalyticsSummaryDTO> getSummaryPost(
            @Valid @RequestBody AnalyticsRequestDTO request
    ) {
        if (request.getFrom().isAfter(request.getTo())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(
                service.getSummary(request.getFrom(), request.getTo(), request.getLimit())
        );
    }
}