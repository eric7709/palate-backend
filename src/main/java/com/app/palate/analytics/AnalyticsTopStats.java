package com.app.palate.analytics;

import java.util.List;

public record AnalyticsTopStats(
        List<TopTable> topTables,
        List<TopRoom> topRooms,
        List<TopCategory> topCategories,
        List<TopItem> topItems,
        List<TopWaiter> topWaiters) {
    public record TopTable(
            Long tableId,
            String tableName,
            int tableNumber,
            double revenue,
            long orderCount,
            double sharePercent,
            double growthPercent) {
    }

    public record TopRoom(
            Long roomId,
            String roomNumber,
            double revenue,
            long orderCount,
            double sharePercent,
            double growthPercent) {
    }

    public record TopCategory(
            Long categoryId,
            String categoryName,
            double revenue,
            long salesCount,
            double sharePercent,
            double growthPercent) {
    }

    public record TopWaiter(
            Long waiterId,
            String waiterName,
            double revenue,
            long orderCount,
            double sharePercent,
            double growthPercent) {
    }
    public record TopItem(
            Long menuItemId,
            String menuItemName,
            String categoryName,
            double revenue,
            long salesCount,
            double sharePercent,
            double growthPercent) {
    }
}