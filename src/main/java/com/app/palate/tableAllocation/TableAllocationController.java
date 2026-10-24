package com.app.palate.tableAllocation;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.app.palate.tableAllocation.dto.TableAllocationResponseDTO;
import com.app.palate.tableAllocation.dto.TableAllocationSearchCriteria;
import com.app.palate.tableAllocation.services.AllocateStaff;
import com.app.palate.tableAllocation.services.DeallocateStaff;
import com.app.palate.tableAllocation.services.GetTableAllocationById;
import com.app.palate.tableAllocation.services.SearchTableAllocations;
import com.app.palate.tableAllocation.utils.TableAllocationMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/palate/table-allocations")
@RequiredArgsConstructor
public class TableAllocationController {
    private final AllocateStaff allocateStaff;
    private final DeallocateStaff deallocateStaff;
    private final SearchTableAllocations searchTableAllocations;
    private final GetTableAllocationById getTableAllocationById;
    private final TableAllocationMapper mapper;

    @PostMapping("/allocate")
    @ResponseStatus(HttpStatus.CREATED)
    public TableAllocationResponseDTO allocate(
            @RequestParam Long tableId,
            @RequestParam Long staffId) {
        return mapper.toDTO(allocateStaff.allocate(tableId, staffId));
    }

    @PostMapping("/deallocate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deallocate(
            @RequestParam Long tableId,
            @RequestParam Long staffId) {
        deallocateStaff.deallocate(tableId, staffId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<TableAllocationResponseDTO> search(TableAllocationSearchCriteria criteria) {
        return searchTableAllocations.search(criteria).map(mapper::toDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TableAllocationResponseDTO get(@PathVariable Long id) {
        return mapper.toDTO(getTableAllocationById.get(id));
    }
}
