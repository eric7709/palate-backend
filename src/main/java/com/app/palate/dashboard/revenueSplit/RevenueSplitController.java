package com.app.palate.dashboard.revenueSplit;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.palate.utils.DashboardPeriod;

@RestController
@RequestMapping("/api/palate/dashboard/revenue-split")
public class RevenueSplitController {

    private final RevenueSplitService revenueSplitService;

    public RevenueSplitController(RevenueSplitService revenueSplitService) {
        this.revenueSplitService = revenueSplitService;
    }

    @GetMapping
    public ResponseEntity<RevenueSplitResponse> getRevenueSplit(
            @RequestParam(defaultValue = "THIS_MONTH") DashboardPeriod period
    ) {
        return ResponseEntity.ok(revenueSplitService.getRevenueSplit(period));
    }
}