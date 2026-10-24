package com.app.palate.restaurantTable.dto;

import com.app.palate.restaurantTable.enums.RestaurantTableStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class RestaurantTableRequestDTO {
    private final String tableName;
    private final Integer tableNumber;
    private final Long waiterId;
    private final Long cashierId;
    private final RestaurantTableStatus status;
    private final Integer capacity;
}
