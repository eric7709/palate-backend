package com.app.palate.category;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
