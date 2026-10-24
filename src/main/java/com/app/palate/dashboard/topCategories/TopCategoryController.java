package com.app.palate.dashboard.topCategories;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.palate.dashboard.topCategories.dto.TopCategoryResponse;
import com.app.palate.dashboard.topCategories.services.GetTopCategories;
import com.app.palate.utils.DashboardPeriod;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/palate/dashboard/top-categories")
public class TopCategoryController {
    private final GetTopCategories getTopCategories;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public TopCategoryResponse get(
            @RequestParam(defaultValue = "THIS_MONTH") DashboardPeriod period,
            @RequestParam(defaultValue = "5") int limit) {
        return getTopCategories.get(period, limit);
    }
}
