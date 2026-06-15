package com.app.palate.dashboard.topMenuItems;

import com.app.palate.utils.DashboardPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/palate/dashboard/top-menu-items")
@RequiredArgsConstructor
public class TopMenuItemController {

    private final TopMenuItemService topMenuItemService;

    @GetMapping
    public ResponseEntity<TopMenuItemResponse> getTopMenuItems(
            @RequestParam(defaultValue = "THIS_MONTH") DashboardPeriod period,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(topMenuItemService.getTopMenuItems(period, limit));
    }
}