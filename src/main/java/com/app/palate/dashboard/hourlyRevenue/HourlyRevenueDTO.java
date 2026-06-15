package com.app.palate.dashboard.hourlyRevenue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyRevenueDTO
 {
    private String hour;      // "8am", "10am", etc.
    private long value;
    private String display;   // formatted currency
    private String color;     // optional, can be set on frontend
}