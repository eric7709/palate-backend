package com.app.palate.tableAllocation;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class TableAllocationResponseDTO {

    private Long id;

    private StaffSummaryDTO cashier;
    private Instant cashierAllocatedAt;
    private Instant cashierDeallocatedAt;

    private StaffSummaryDTO waiter;
    private Instant waiterAllocatedAt;
    private Instant waiterDeallocatedAt;

    @Getter
    @Builder
    public static class StaffSummaryDTO {
        private Long id;
        private String fullName;
    }
}