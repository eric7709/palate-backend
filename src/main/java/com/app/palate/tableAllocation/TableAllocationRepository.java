package com.app.palate.tableAllocation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableAllocationRepository
                extends JpaRepository<TableAllocation, Long>,
                JpaSpecificationExecutor<TableAllocation> {

        List<TableAllocation> findByTableIdAndCashierIsNotNullAndCashierDeallocatedAtIsNull(Long tableId);

        List<TableAllocation> findByTableIdAndWaiterIsNotNullAndWaiterDeallocatedAtIsNull(Long tableId);

        @Query("SELECT a FROM TableAllocation a WHERE a.table.id = :tableId AND (a.cashierDeallocatedAt IS NULL OR a.waiterDeallocatedAt IS NULL)")
        Optional<TableAllocation> findOpenAllocationByTableId(@Param("tableId") Long tableId);
}