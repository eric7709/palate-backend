package com.app.palate.auth.dto;

import com.app.palate.auth.enums.AccountStatus;
import com.app.palate.auth.enums.Gender;
import com.app.palate.auth.enums.Role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Query parameters for listing employees: what to look for, which filters, which page, and how to sort. */
@Getter
@Setter
@NoArgsConstructor
public class EmployeeSearchCriteria {
    private String search;
    private Role role;
    private AccountStatus status;
    private Gender gender;
    private int page = 0;
    private int size = 30;
    private String sortBy = "id";
    private String sortDirection = "desc";
}
