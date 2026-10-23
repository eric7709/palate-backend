package com.app.palate.menuItem;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
