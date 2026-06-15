package com.app.palate.dashboard.quickStats;

import com.app.palate.utils.DashboardPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/palate/dashboard/quick-stats")
@RequiredArgsConstructor
public class QuickStatsController {

    private final QuickStatsService quickStatsService;

    @GetMapping
    public ResponseEntity<QuickStatsResponse> getQuickStats(
            @RequestParam(defaultValue = "TODAY") DashboardPeriod period
    ) {
        return ResponseEntity.ok(quickStatsService.getQuickStats(period));
    }
}