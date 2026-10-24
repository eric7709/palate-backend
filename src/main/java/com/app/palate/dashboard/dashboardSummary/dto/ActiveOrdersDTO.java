package com.app.palate.dashboard.dashboardSummary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveOrdersDTO {
     private long pending;
    private long paid;
}
