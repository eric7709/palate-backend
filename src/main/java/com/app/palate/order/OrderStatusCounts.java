package com.app.palate.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusCounts {
    private long pending;
    private long preparing;
    private long completed;
    private long paid;
    private long cancelled;
    private double total;
}
