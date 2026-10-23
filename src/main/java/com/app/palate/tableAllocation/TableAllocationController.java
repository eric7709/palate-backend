package com.app.palate.tableAllocation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/palate/table-allocations")
@RequiredArgsConstructor
public class TableAllocationController {

    private final TableAllocationService allocationService;
    private final TableAllocationMapper mapper;

    @PostMapping("/allocate")
    @ResponseStatus(HttpStatus.CREATED)
    public TableAllocationResponseDTO allocateStaff(
            @RequestParam Long tableId,
            @RequestParam Long staffId) {
        return mapper.toDTO(allocationService.allocateStaff(tableId, staffId));
    }

    @PostMapping("/deallocate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deallocateStaff(
            @RequestParam Long tableId,
            @RequestParam Long staffId) {

        allocationService.deallocateStaff(tableId, staffId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<TableAllocationResponseDTO> getAllAllocations(TableAllocationSearchCriteria criteria) {
        return allocationService.getAllAllocations(criteria).map(mapper::toDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TableAllocationResponseDTO getAllocationById(@PathVariable Long id) {
        return mapper.toDTO(allocationService.getAllocationById(id));
    }
}