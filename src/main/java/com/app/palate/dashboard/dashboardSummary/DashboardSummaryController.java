package com.app.palate.dashboard.dashboardSummary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/palate/dashboard/summary")
@RequiredArgsConstructor
public class DashboardSummaryController {

    private final DashboardSummaryService dashboardSummaryService;
    
    @GetMapping
    public ResponseEntity<DashboardSummaryDTO> getSummary(
            @RequestParam(defaultValue = "THIS_MONTH") DashboardPeriod period) {
        return ResponseEntity.ok(dashboardSummaryService.getSummary(period));
    }

}
