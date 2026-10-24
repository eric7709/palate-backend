package com.app.palate.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopWaiterDTO {
    private Long waiterId;
    private String firstName;
    private String lastName;
    private Double totalSales;
}
