package com.app.palate.restaurantTable.dto;

import com.app.palate.restaurantTable.enums.RestaurantTableStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Query parameters for listing tables: what to look for, which filters, which page, and how to sort. */
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
