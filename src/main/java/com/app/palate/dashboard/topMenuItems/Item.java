package com.app.palate.dashboard.topMenuItems;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private int rank;
    private String name;
    private String value; // formatted revenue, e.g. "₦142K"
    private int pct; // percentage of top item's revenue (100% for first)
    private String color; // optional, frontend can set
}
