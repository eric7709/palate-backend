package com.app.palate.tableAllocation.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Query parameters for listing allocations: what to look for, which filters, which page, and how to sort. */
@Getter
@Setter
@NoArgsConstructor
public class TableAllocationSearchCriteria {
    private String search;
    private Long tableId;
    private Long staffId;
    private String role;
    private Boolean active;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;
    private int page = 0;
    private int size = 10;
    private String sortBy = "cashierAllocatedAt";
    private String sortDirection = "desc";
}
