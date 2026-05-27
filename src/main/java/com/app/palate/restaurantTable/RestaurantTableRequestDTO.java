package com.app.palate.restaurantTable;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

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