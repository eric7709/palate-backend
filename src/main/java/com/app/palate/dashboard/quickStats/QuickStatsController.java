package com.app.palate.dashboard.quickStats;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.palate.dashboard.quickStats.dto.QuickStatsResponse;
import com.app.palate.dashboard.quickStats.services.GetQuickStats;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/palate/dashboard/quick-stats")
public class QuickStatsController {
    private final GetQuickStats getQuickStats;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public QuickStatsResponse get(@RequestParam(defaultValue = "TODAY") DashboardPeriod period) {
        return getQuickStats.get(period);
    }
}
