package com.app.palate.category.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class CategoryResponseDTO {
    private final Long id;
    private final String name;
    private final String description;
    private final String status;
    private final long menuItemCount;
}
