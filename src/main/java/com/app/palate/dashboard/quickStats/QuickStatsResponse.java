package com.app.palate.dashboard.quickStats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickStatsResponse {
    private List<StatItem> stats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatItem {
        private String label;
        private String value;
        private String sub;
    }
}