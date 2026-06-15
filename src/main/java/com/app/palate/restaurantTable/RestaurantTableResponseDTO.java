package com.app.palate.restaurantTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RestaurantTableResponseDTO {
    private Long id;
    private String tableName;
    private Integer tableNumber;
    private String status;
    private Integer capacity;
    private String qrCode; // <-- Added field
    private String waiterName;
    private Long waiterId;
    private String cashierName;
    private Long cashierId;
}