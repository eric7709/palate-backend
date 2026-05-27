package com.app.palate.tableAllocation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/table-allocations")
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
    public Page<TableAllocationResponseDTO> getAllAllocations(
            @RequestParam(required = false) Long tableId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "cashierAllocatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        return allocationService.getAllAllocations(
                tableId, staffId, role, active, date, page, size, sortBy, sortDirection
        ).map(mapper::toDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TableAllocationResponseDTO getAllocationById(@PathVariable Long id) {
        return mapper.toDTO(allocationService.getAllocationById(id));
    }
}