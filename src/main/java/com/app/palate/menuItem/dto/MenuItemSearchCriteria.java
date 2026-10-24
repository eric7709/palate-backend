package com.app.palate.menuItem.dto;

import com.app.palate.menuItem.enums.MenuItemStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Query parameters for listing menu items: what to look for, which filters, which page, and how to sort. */
@Getter
@Setter
@NoArgsConstructor
public class MenuItemSearchCriteria {
    private String search;
    private Long categoryId;
    private MenuItemStatus status;
    private Boolean isAvailable;
    private Double minPrice;
    private Double maxPrice;
    private int page = 0;
    private int size = 100;
    private String sortBy = "id";
    private String sortDirection = "desc";
}
