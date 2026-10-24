package com.app.palate.analytics.utils;

import static com.app.palate.analytics.utils.RowValues.toDouble;
import static com.app.palate.analytics.utils.RowValues.toInt;
import static com.app.palate.analytics.utils.RowValues.toLocalDate;
import static com.app.palate.analytics.utils.RowValues.toLong;

import java.util.List;

import org.springframework.stereotype.Component;

import com.app.palate.analytics.dto.AccountSalesDTO;
import com.app.palate.analytics.dto.CategorySalesDTO;
import com.app.palate.analytics.dto.CustomerSalesDTO;
import com.app.palate.analytics.dto.DailyRevenueDTO;
import com.app.palate.analytics.dto.DaySalesDTO;
import com.app.palate.analytics.dto.HourSalesDTO;
import com.app.palate.analytics.dto.MenuItemSalesDTO;
import com.app.palate.analytics.dto.RoomSalesDTO;
import com.app.palate.analytics.dto.TableSalesDTO;

/** Turns the raw rows the analytics queries return into response DTOs. */
@Component
public class AnalyticsRowMapper {

    // [id, name, totalSales, orderCount]
    public List<AccountSalesDTO> toAccountSales(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new AccountSalesDTO(toLong(r[0]), (String) r[1], toDouble(r[2]), toLong(r[3])))
                .toList();
    }

    // [id, tableName, tableNumber, totalSales, orderCount]
    public List<TableSalesDTO> toTableSales(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new TableSalesDTO(toLong(r[0]), (String) r[1], toInt(r[2]), toDouble(r[3]), toLong(r[4])))
                .toList();
    }

    // [id, roomNumber, totalSales, orderCount]
    public List<RoomSalesDTO> toRoomSales(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new RoomSalesDTO(toLong(r[0]), (String) r[1], toDouble(r[2]), toLong(r[3])))
                .toList();
    }

    // [id, name, totalSales, totalQuantity]
    public List<CategorySalesDTO> toCategorySales(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new CategorySalesDTO(toLong(r[0]), (String) r[1], toDouble(r[2]), toLong(r[3])))
                .toList();
    }

    // [id, name, categoryName, totalSales, totalQuantity]
    public List<MenuItemSalesDTO> toMenuItemSales(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new MenuItemSalesDTO(toLong(r[0]), (String) r[1], (String) r[2], toDouble(r[3]),
                        toLong(r[4])))
                .toList();
    }

    // [id, name, phoneNumber, totalSales, orderCount]
    public List<CustomerSalesDTO> toCustomerSales(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new CustomerSalesDTO(toLong(r[0]), (String) r[1], (String) r[2], toDouble(r[3]),
                        toLong(r[4])))
                .toList();
    }

    // [dayName, orderCount, totalSales]
    public List<DaySalesDTO> toDaySales(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new DaySalesDTO(((String) r[0]).trim(), toLong(r[1]), toDouble(r[2])))
                .toList();
    }

    // [hour, orderCount, totalSales]
    public List<HourSalesDTO> toHourSales(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new HourSalesDTO(toInt(r[0]), toLong(r[1]), toDouble(r[2])))
                .toList();
    }

    // [date, orderCount, totalSales]
    public List<DailyRevenueDTO> toDailyRevenue(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new DailyRevenueDTO(toLocalDate(r[0]), toLong(r[1]), toDouble(r[2])))
                .toList();
    }
}
