package com.app.palate.dashboard;

import java.util.List;

public record DashboardTopStats(
    List<TopTable>    topTables,
    List<TopCategory> topCategories,
    List<TopItem>     topItems,
    List<TopWaiter>   topWaiters
) {
    public record TopTable(
        Long   tableId,
        String tableName,
        int    tableNumber,
        double revenue,
        long   orderCount,
        double utilizationPercent,
        double growthPercent
    ) {}

    public record TopCategory(
        Long   categoryId,
        String categoryName,
        double revenue,
        long   salesCount,
        double growthPercent
    ) {}

    public record TopItem(
        Long   menuItemId,
        String menuItemName,
        String categoryName,
        double revenue,
        long   salesCount,
        double growthPercent
    ) {}

    public record TopWaiter(
        Long   waiterId,
        String waiterName,
        double revenue,
        long   orderCount,
        double growthPercent
    ) {}
}