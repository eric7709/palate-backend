package com.app.palate.tableAllocation.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.tableAllocation.TableAllocation;
import com.app.palate.tableAllocation.TableAllocationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetTableAllocationById {
    private final TableAllocationRepository tableAllocationRepository;

    @Transactional(readOnly = true)
    public TableAllocation get(Long id) {
        if(id == null) throw new BadRequestException("Table Id is required");
        return tableAllocationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Allocation not found"));
    }
}
