package com.app.palate.restaurantTable.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTopTableDTO {
    private Long tableId;
    private String tableName;
    private String tableNumber;
    private Double totalSales;
}
