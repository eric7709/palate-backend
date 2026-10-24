package com.app.palate.dashboard.revenueSplit;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.palate.dashboard.revenueSplit.dto.RevenueSplitResponse;
import com.app.palate.dashboard.revenueSplit.services.GetRevenueSplit;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/palate/dashboard/revenue-split")
public class RevenueSplitController {
    private final GetRevenueSplit getRevenueSplit;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public RevenueSplitResponse get(@RequestParam(defaultValue = "THIS_MONTH") DashboardPeriod period) {
        return getRevenueSplit.get(period);
    }
}
