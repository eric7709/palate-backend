package com.app.palate.tableAllocation.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class TableAllocationRequestDTO {
    private final Long tableId;
    private final Long waiterId;
}
