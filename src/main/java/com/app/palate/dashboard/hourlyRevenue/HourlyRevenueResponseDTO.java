package com.app.palate.dashboard.hourlyRevenue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyRevenueResponseDTO {
    private List<HourlyRevenueDTO> data;
    private long totalRevenue;
    private long previousTotal;
    private double changePercent;
}