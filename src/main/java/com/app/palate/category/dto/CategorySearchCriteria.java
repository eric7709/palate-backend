package com.app.palate.category.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Query parameters for listing categories: what to look for, which page, and how to sort. */
@Getter
@Setter
@NoArgsConstructor
public class CategorySearchCriteria {
    private String search;
    private String status;
    private int page = 0;
    private int size = 50;
    private String sortBy = "id";
    private String sortDirection = "desc";
}
