package com.app.palate.recommendation;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.palate.menuItem.MenuItem;
import com.app.palate.menuItem.dto.MenuItemResponseDTO;
import com.app.palate.menuItem.utils.MenuItemMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/palate/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final MenuItemRecommendationService recommendationService;
    private final MenuItemMapper menuItemMapper;

    /**
     * GET /api/palate/recommendations?customerId=123&limit=20
     * GET /api/palate/recommendations                        (guest / first-time customer)
     *
     * Returns a ranked list of available menu items recommended for the
     * given customer. customerId is optional — a first-time or guest
     * customer with no identity yet can call this with no id at all, and
     * still gets a full list back, ranked by overall popularity instead
     * of personal history. limit is optional too; the service falls back
     * to its own default when it's missing, zero, or negative.
     */
    @GetMapping
    public List<MenuItemResponseDTO> getRecommendations(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Integer limit) {

        int rawLimit = (limit != null) ? limit : 0;

        List<MenuItem> recommendedItems = recommendationService.recommend(customerId, rawLimit);

        return recommendedItems.stream()
                .map(menuItemMapper::toResponse)
                .toList();
    }
}