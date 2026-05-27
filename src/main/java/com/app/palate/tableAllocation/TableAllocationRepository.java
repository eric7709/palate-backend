package com.app.palate.tableAllocation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableAllocationRepository
        extends JpaRepository<TableAllocation, Long>,
                JpaSpecificationExecutor<TableAllocation> {

    List<TableAllocation> findByTableIdAndCashierIsNotNullAndCashierDeallocatedAtIsNull(Long tableId);

    List<TableAllocation> findByTableIdAndWaiterIsNotNullAndWaiterDeallocatedAtIsNull(Long tableId);
}