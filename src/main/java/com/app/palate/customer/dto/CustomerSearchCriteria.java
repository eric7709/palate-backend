package com.app.palate.customer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Query parameters for searching customer fields and controlling pagination and sorting. */
@Getter
@Setter
@NoArgsConstructor
public class CustomerSearchCriteria {
    /** Matched against customer fields, including name, title, email, and phone number. */
    private String search;
    private int page = 0;
    private int size = 10;
    private String sortBy = "id";
    private String sortDirection = "asc";
}
