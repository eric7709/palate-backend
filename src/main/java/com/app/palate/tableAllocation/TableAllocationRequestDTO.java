package com.app.palate.tableAllocation;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

@Getter
@RequiredArgsConstructor
public final class TableAllocationRequestDTO {
    private final Long tableId;
    private final Long waiterId;
}