package com.app.palate.dashboard.dashboardSummary;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.palate.dashboard.dashboardSummary.dto.DashboardSummaryDTO;
import com.app.palate.dashboard.dashboardSummary.services.GetDashboardSummary;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/palate/dashboard/summary")
public class DashboardSummaryController {
    private final GetDashboardSummary getDashboardSummary;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public DashboardSummaryDTO get(@RequestParam(defaultValue = "THIS_MONTH") DashboardPeriod period) {
        return getDashboardSummary.get(period);
    }
}
