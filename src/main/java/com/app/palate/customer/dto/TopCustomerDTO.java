package com.app.palate.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopCustomerDTO {
    private Long customerId;
    private String customerName;
    private Double totalSales;
}
