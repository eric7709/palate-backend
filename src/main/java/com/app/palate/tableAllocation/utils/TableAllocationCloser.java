package com.app.palate.tableAllocation.utils;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.app.palate.tableAllocation.TableAllocationRepository;

import lombok.RequiredArgsConstructor;

/** Stamps the end time on a table's currently open cashier or waiter allocations. */
@Component
@RequiredArgsConstructor
public class TableAllocationCloser {
    private final TableAllocationRepository tableAllocationRepository;

    public void closeCashier(Long tableId) {
        tableAllocationRepository
                .findByTableIdAndCashierIsNotNullAndCashierDeallocatedAtIsNull(tableId)
                .forEach(alloc -> alloc.setCashierDeallocatedAt(Instant.now()));
    }

    public void closeWaiter(Long tableId) {
        tableAllocationRepository
                .findByTableIdAndWaiterIsNotNullAndWaiterDeallocatedAtIsNull(tableId)
                .forEach(alloc -> alloc.setWaiterDeallocatedAt(Instant.now()));
    }
}
