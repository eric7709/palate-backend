package com.app.palate.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeSearchCriteria {
    private String search;
    private String role;
    private String status;
    private String gender;
    private int page = 0;
    private int size = 30;
    private String sortBy = "id";
    private String sortDirection = "desc";
}
