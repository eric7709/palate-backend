package com.app.palate.dashboard.topCategories;

import com.app.palate.utils.DashboardPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/palate/dashboard/top-categories")
@RequiredArgsConstructor
public class TopCategoryController {

    private final TopCategoryService topCategoryService;

    @GetMapping
    public ResponseEntity<TopCategoryResponse> getTopCategories(
            @RequestParam(defaultValue = "THIS_MONTH") DashboardPeriod period,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(topCategoryService.getTopCategories(period, limit));
    }
}