package com.app.palate.customer;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopCustomerDTO {
    private Long customerId;
    private String customerName;
    private Double totalSales;
}

