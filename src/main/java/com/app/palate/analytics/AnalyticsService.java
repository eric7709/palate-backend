package com.app.palate.analytics;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.order.OrderRepository;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final OrderRepository orderRepository;

    public Map<String, Object> getBusinessIntelligenceReport(String startDate, String endDate) {
        ValidationUtils.requireNonBlank(startDate, "Start date");
        ValidationUtils.requireNonBlank(endDate, "End date");

        try {
            // Start of the day (00:00:00)
            Instant start = Instant.parse(startDate.trim() + "T00:00:00Z");
            // End of the day (23:59:59)
            Instant end = Instant.parse(endDate.trim() + "T23:59:59Z");

            if (end.isBefore(start)) {
                throw new BadRequestException("End date cannot be earlier than start date");
            }

            return getFullBusinessIntelligence(start, end);

        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date format. Expected format: YYYY-MM-DD");
        }
    }

    private Map<String, Object> getFullBusinessIntelligence(Instant start, Instant end) {
        Map<String, Object> db = new LinkedHashMap<>();

        // 1. Executive Summary
        Object[] totalsResult = orderRepository.getGlobalTotals(start, end);
        Object[] totals = (totalsResult != null && totalsResult.length > 0) ? (Object[]) totalsResult[0] : new Object[]{0, 0.0};
        
        db.put("summary", Map.of(
            "totalOrders", totals[0] != null ? totals[0] : 0,
            "totalRevenue", totals[1] != null ? totals[1] : 0.0
        ));

        // 2. Map all lists using universal keys: label, quantity, revenue
        db.put("shifts", mapData(orderRepository.getSalesByShift(start, end)));
        db.put("days", mapData(orderRepository.getSalesByDayOfWeek(start, end)));
        db.put("menuItems", mapData(orderRepository.getMenuItemStats(start, end)));
        db.put("categories", mapData(orderRepository.getCategoryStats(start, end)));
        db.put("waiters", mapData(orderRepository.getWaiterStats(start, end)));
        db.put("tables", mapData(orderRepository.getTableStats(start, end)));
        db.put("customers", mapData(orderRepository.getCustomerStats(start, end)));

        return db;
    }

    /**
     * Standardizes all Object[] lists into a consistent List of Maps for the frontend.
     */
    private List<Map<String, Object>> mapData(List<Object[]> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (list == null) return result;
        
        for (Object[] row : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("label", row[0]);    // Name, Shift, or Day
            m.put("quantity", row[1]); // Count or Quantity
            m.put("revenue", row[2]);  // Total Revenue
            result.add(m);
        }
        return result;
    }
}