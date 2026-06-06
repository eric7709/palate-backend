package com.app.palate.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository repo;

    public AnalyticsSummaryDTO getSummary(LocalDate from, LocalDate to, int limit) {
        Instant start = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end   = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        // ── KPIs ─────────────────────────────────────────────────────────────
        double totalRevenue      = nullSafe(repo.totalRevenue(start, end));
        long   totalOrders       = nullSafeLong(repo.totalOrderCount(start, end));
        long   cancelled         = nullSafeLong(repo.cancelledOrderCount(start, end));
        long   totalPlaced       = nullSafeLong(repo.totalOrdersPlaced(start, end));
        double avgOrderValue     = nullSafe(repo.averageOrderValue(start, end));
        double avgItems          = nullSafe(repo.averageItemsPerOrder(start, end));
        double cancellationRate  = totalPlaced > 0 ? (double) cancelled / totalPlaced * 100 : 0.0;

        // ── New vs returning ──────────────────────────────────────────────────
        List<Object[]> crRows        = repo.newVsReturningCustomers(start, end);
        Object[]       cr            = crRows.isEmpty() ? new Object[]{0L, 0L} : crRows.get(0);
        long           newCustomers  = toLong(cr[0]);
        long           returning     = toLong(cr[1]);

        // ── Take-out vs dine-in ───────────────────────────────────────────────
        long   takeOutCount   = 0, dineInCount   = 0;
        double takeOutRevenue = 0, dineInRevenue = 0;
        for (Object[] r : repo.takeOutVsDineIn(start, end)) {
            boolean isTakeOut = (Boolean) r[0];
            if (isTakeOut) { takeOutCount = toLong(r[1]);  takeOutRevenue = toDouble(r[2]); }
            else           { dineInCount  = toLong(r[1]);  dineInRevenue  = toDouble(r[2]); }
        }

        return AnalyticsSummaryDTO.builder()
                // KPIs
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(avgOrderValue)
                .averageItemsPerOrder(avgItems)
                .cancelledOrders(cancelled)
                .cancellationRate(cancellationRate)
                .newCustomers(newCustomers)
                .returningCustomers(returning)

                // Top by sales
                .topWaitersBySales(cap(mapAccount(repo.topWaitersBySales(start, end)), limit))
                .topCashiersBySales(cap(mapAccount(repo.topCashiersBySales(start, end)), limit))
                .topTablesBySales(cap(mapTable(repo.topTablesBySales(start, end)), limit))
                .topCategoriesBySales(cap(mapCategory(repo.topCategoriesBySales(start, end)), limit))
                .topMenuItemsBySales(cap(mapMenuItem(repo.topMenuItemsBySales(start, end)), limit))
                .topCustomersBySales(cap(mapCustomer(repo.topCustomersBySales(start, end)), limit))

                // Top by count
                .topWaitersByCount(cap(mapAccount(repo.topWaitersByCount(start, end)), limit))
                .topCashiersByCount(cap(mapAccount(repo.topCashiersByCount(start, end)), limit))
                .topTablesByCount(cap(mapTable(repo.topTablesByCount(start, end)), limit))
                .topCategoriesByCount(cap(mapCategory(repo.topCategoriesByCount(start, end)), limit))
                .topMenuItemsByCount(cap(mapMenuItem(repo.topMenuItemsByCount(start, end)), limit))
                .topCustomersByCount(cap(mapCustomer(repo.topCustomersByCount(start, end)), limit))
                // topCustomersByFrequency removed — was duplicate of topCustomersByCount

                // Least by sales
                .leastWaitersBySales(cap(mapAccount(repo.leastWaitersBySales(start, end)), limit))
                .leastCashiersBySales(cap(mapAccount(repo.leastCashiersBySales(start, end)), limit))
                .leastTablesBySales(cap(mapTable(repo.leastTablesBySales(start, end)), limit))
                .leastCategoriesBySales(cap(mapCategory(repo.leastCategoriesBySales(start, end)), limit))
                .leastMenuItemsBySales(cap(mapMenuItem(repo.leastMenuItemsBySales(start, end)), limit))

                // Least by count
                .leastWaitersByCount(cap(mapAccount(repo.leastWaitersByCount(start, end)), limit))
                .leastCashiersByCount(cap(mapAccount(repo.leastCashiersByCount(start, end)), limit))
                .leastTablesByCount(cap(mapTable(repo.leastTablesByCount(start, end)), limit))
                .leastCategoriesByCount(cap(mapCategory(repo.leastCategoriesByCount(start, end)), limit))
                .leastMenuItemsByCount(cap(mapMenuItem(repo.leastMenuItemsByCount(start, end)), limit))

                // Time-series
                .salesByDay(repo.salesByDayOfWeek(start, end).stream()
                        .map(r -> new DaySalesDTO(((String) r[0]).trim(), toLong(r[1]), toDouble(r[2])))
                        .toList())
                .salesByHour(repo.salesByHour(start, end).stream()
                        .map(r -> new HourSalesDTO(toInt(r[0]), toLong(r[1]), toDouble(r[2])))
                        .toList())
                .revenueOverTime(repo.revenueOverTime(start, end).stream()
                        .map(r -> new DailyRevenueDTO(toLocalDate(r[0]), toLong(r[1]), toDouble(r[2])))
                        .toList())

                // Take-out vs dine-in
                .takeOutCount(takeOutCount)
                .takeOutRevenue(takeOutRevenue)
                .dineInCount(dineInCount)
                .dineInRevenue(dineInRevenue)
                .build();
    }

    // ─── Mappers ──────────────────────────────────────────────────────────────

    // [id, name, totalSales, orderCount]
    private List<AccountSalesDTO> mapAccount(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new AccountSalesDTO(toLong(r[0]), (String) r[1], toDouble(r[2]), toLong(r[3])))
                .toList();
    }

    // [id, tableName, tableNumber, totalSales, orderCount]
    private List<TableSalesDTO> mapTable(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new TableSalesDTO(toLong(r[0]), (String) r[1], toInt(r[2]), toDouble(r[3]), toLong(r[4])))
                .toList();
    }

    // [id, name, totalSales, totalQuantity]
    private List<CategorySalesDTO> mapCategory(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new CategorySalesDTO(toLong(r[0]), (String) r[1], toDouble(r[2]), toLong(r[3])))
                .toList();
    }

    // [id, name, categoryName, totalSales, totalQuantity]
    private List<MenuItemSalesDTO> mapMenuItem(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new MenuItemSalesDTO(toLong(r[0]), (String) r[1], (String) r[2], toDouble(r[3]), toLong(r[4])))
                .toList();
    }

    // [id, name, phoneNumber, totalSales, orderCount]
    private List<CustomerSalesDTO> mapCustomer(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new CustomerSalesDTO(toLong(r[0]), (String) r[1], (String) r[2], toDouble(r[3]), toLong(r[4])))
                .toList();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private <T> List<T> cap(List<T> list, int limit) {
        return list.size() <= limit ? list : list.subList(0, limit);
    }

    private double nullSafe(Double val)     { return val != null ? val : 0.0; }
    private long   nullSafeLong(Long val)   { return val != null ? val : 0L;  }

    private long toLong(Object val) {
        if (val == null)             return 0L;
        if (val instanceof Long l)   return l;
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }

    private double toDouble(Object val) {
        if (val == null)                  return 0.0;
        if (val instanceof Double d)      return d;
        if (val instanceof BigDecimal b)  return b.doubleValue();
        if (val instanceof Number n)      return n.doubleValue();
        return Double.parseDouble(val.toString());
    }

    private int toInt(Object val) {
        if (val == null)               return 0;
        if (val instanceof Integer i)  return i;
        if (val instanceof Number n)   return n.intValue();
        return Integer.parseInt(val.toString());
    }

    private LocalDate toLocalDate(Object val) {
        if (val instanceof java.sql.Date d) return d.toLocalDate();
        if (val instanceof LocalDate ld)    return ld;
        return LocalDate.parse(val.toString());
    }
}