package com.app.palate.dashboard.topMenuItems;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.palate.dashboard.topMenuItems.dto.TopMenuItemResponse;
import com.app.palate.dashboard.topMenuItems.services.GetTopMenuItems;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/palate/dashboard/top-menu-items")
public class TopMenuItemController {
    private final GetTopMenuItems getTopMenuItems;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public TopMenuItemResponse get(
            @RequestParam(defaultValue = "THIS_MONTH") DashboardPeriod period,
            @RequestParam(defaultValue = "10") int limit) {
        return getTopMenuItems.get(period, limit);
    }
}
