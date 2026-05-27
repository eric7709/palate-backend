package com.app.palate.analytics;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/palate/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getBusinessIntelligence(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
            
        return analyticsService.getBusinessIntelligenceReport(startDate, endDate);
    }
}