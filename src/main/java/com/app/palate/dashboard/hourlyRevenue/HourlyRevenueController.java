package com.app.palate.dashboard.hourlyRevenue;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.palate.utils.DashboardPeriod;

@RestController
@RequestMapping("/api/palate/dashboard/hourly-revenue")
@RequiredArgsConstructor
public class HourlyRevenueController {

    private final HourlyRevenueService hourlyRevenueService;

    @GetMapping
    public ResponseEntity<HourlyRevenueResponseDTO> getHourlyRevenue(
            @RequestParam(defaultValue = "TODAY") DashboardPeriod period) {
        return ResponseEntity.ok(hourlyRevenueService.getHourlyRevenue(period));
    }
}