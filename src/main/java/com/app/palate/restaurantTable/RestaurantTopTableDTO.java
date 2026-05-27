package com.app.palate.restaurantTable;

import lombok.*;

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
