package com.app.palate.restaurantTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RestaurantTableSearchCriteria {
    private String search;
    private RestaurantTableStatus status;
    private Integer tableNumber;
    private Integer capacity;
    private Long waiterId;
    private Long cashierId;
    private int page = 0;
    private int size = 50;
    private String sortBy = "id";
    private String sortDirection = "desc";
}
