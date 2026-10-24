package com.app.palate.dashboard.dashboardSummary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemsDTO {
    private long active;
    private long unavailable;
}
