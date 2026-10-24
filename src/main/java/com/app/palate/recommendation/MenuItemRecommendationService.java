package com.app.palate.recommendation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.app.palate.menuItem.MenuItem;
import com.app.palate.menuItem.MenuItemRepository;
import com.app.palate.menuItem.enums.MenuItemStatus;
import com.app.palate.order.Order;
import com.app.palate.order.OrderRepository;
import com.app.palate.orderItem.OrderItem;

import lombok.RequiredArgsConstructor;

/**
 * Ranks available menu items for a given customer using a weighted
 * combination of three signals, matching R(u,i) = alpha*F(u,i) + beta*C(u,c) + gamma*P(i):
 *
 *   F(u,i) - how often this customer has ordered this exact item
 *   C(u,c) - how often this customer has ordered from this item's category
 *   P(i)   - how popular this item is across all customers
 *
 * A customer with no order history simply gets F = C = 0 for every item,
 * so the ranking naturally falls back to category/popularity evidence
 * instead of failing or throwing for new customers.
 */
@Service
@RequiredArgsConstructor
public class MenuItemRecommendationService {

    private static final double ALPHA = 0.5; // weight given to the customer's own item history
    private static final double BETA = 0.3;  // weight given to the customer's category history
    private static final double GAMMA = 0.2; // weight given to overall item popularity
    private static final int DEFAULT_LIMIT = 50; // used whenever limit is missing, zero, or negative

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;

    /**
     * Returns the top {@code limit} available menu items recommended for
     * {@code customerId}, ordered from strongest to weakest match.
     *
     * {@code customerId} may be {@code null} — for a first-time or guest
     * customer with no identity yet, the item- and category-frequency
     * signals simply contribute 0, and the ranking falls back to pure
     * item popularity across all customers.
     *
     * {@code limit} of 0 or less (e.g. not supplied by the caller) is
     * treated as {@link #DEFAULT_LIMIT} rather than rejected.
     */
    @SuppressWarnings("null")
    public List<MenuItem> recommend(Long customerId, int limit) {
        int resolvedLimit = limit > 0 ? limit : DEFAULT_LIMIT;

        List<MenuItem> availableItems = menuItemRepository.findAll().stream()
                .filter(item -> item.getStatus() == MenuItemStatus.AVAILABLE)
                .toList();
        if (availableItems.isEmpty()) {
            return List.of();
        }

        List<Order> customerOrders = (customerId != null)
                ? orderRepository.fetchCustomerOrders(customerId)
                : List.of();
        List<Order> allOrders = orderRepository.findAll();

        Map<Long, Integer> customerItemFrequency = countItemFrequency(customerOrders);
        Map<Long, Integer> customerCategoryFrequency = countCategoryFrequency(customerOrders);
        Map<Long, Integer> overallItemPopularity = countItemFrequency(allOrders);

        int maxItemFrequency = maxValue(customerItemFrequency);
        int maxCategoryFrequency = maxValue(customerCategoryFrequency);
        int maxPopularity = maxValue(overallItemPopularity);

        List<ScoredMenuItem> scoredItems = new ArrayList<>();

        for (MenuItem item : availableItems) {
            double itemFrequencyScore = normalize(
                    customerItemFrequency.getOrDefault(item.getId(), 0), maxItemFrequency);

            double categoryFrequencyScore = (item.getCategory() != null)
                    ? normalize(
                            customerCategoryFrequency.getOrDefault(item.getCategory().getId(), 0),
                            maxCategoryFrequency)
                    : 0.0;

            double popularityScore = normalize(
                    overallItemPopularity.getOrDefault(item.getId(), 0), maxPopularity);

            double score = (ALPHA * itemFrequencyScore)
                    + (BETA * categoryFrequencyScore)
                    + (GAMMA * popularityScore);

            scoredItems.add(new ScoredMenuItem(item, score));
        }

        return scoredItems.stream()
                .sorted(Comparator.comparingDouble(ScoredMenuItem::score).reversed())
                .limit(resolvedLimit)
                .map(ScoredMenuItem::menuItem)
                .toList();
    }

    // --- Frequency counting -------------------------------------------------

    
    /** Counts total quantity ordered per menu item across the given orders. */
    @SuppressWarnings("null")
    private Map<Long, Integer> countItemFrequency(List<Order> orders) {
        Map<Long, Integer> counts = new HashMap<>();
        for (Order order : orders) {
            if (order.getItems() == null) {
                continue;
            }
            for (OrderItem orderItem : order.getItems()) {
                if (orderItem.getMenuItem() == null) {
                    continue;
                }
                Long menuItemId = orderItem.getMenuItem().getId();
                counts.merge(menuItemId, orderItem.getQuantity(), Integer::sum);
            }
        }
        return counts;
    }

    /** Counts total quantity ordered per menu category across the given orders. */
    @SuppressWarnings("null")
    private Map<Long, Integer> countCategoryFrequency(List<Order> orders) {
        Map<Long, Integer> counts = new HashMap<>();
        for (Order order : orders) {
            if (order.getItems() == null) {
                continue;
            }
            for (OrderItem orderItem : order.getItems()) {
                MenuItem menuItem = orderItem.getMenuItem();
                if (menuItem == null || menuItem.getCategory() == null) {
                    continue;
                }
                Long categoryId = menuItem.getCategory().getId();
                counts.merge(categoryId, orderItem.getQuantity(), Integer::sum);
            }
        }
        return counts;
    }

    // --- Helpers --------------------------------------------------------------
    @SuppressWarnings("null")
    private int maxValue(Map<Long, Integer> counts) {
        return counts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    /** Scales a raw count into a 0.0-1.0 range relative to the highest count seen. */
    private double normalize(int value, int max) {
        return max == 0 ? 0.0 : (double) value / max;
    }

    private record ScoredMenuItem(MenuItem menuItem, double score) {
    }
}