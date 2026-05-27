package com.app.palate.auth;

import lombok.*;

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