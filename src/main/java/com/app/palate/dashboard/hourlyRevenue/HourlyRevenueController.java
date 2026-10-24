package com.app.palate.dashboard.hourlyRevenue;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.palate.dashboard.hourlyRevenue.dto.HourlyRevenueResponseDTO;
import com.app.palate.dashboard.hourlyRevenue.services.GetHourlyRevenue;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/palate/dashboard/hourly-revenue")
public class HourlyRevenueController {
    private final GetHourlyRevenue getHourlyRevenue;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public HourlyRevenueResponseDTO get(@RequestParam(defaultValue = "TODAY") DashboardPeriod period) {
        return getHourlyRevenue.get(period);
    }
}
