package com.app.palate.category;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

@Getter
@RequiredArgsConstructor
public final class CategoryResponseDTO {
    private final Long id;
    private final String name;
    private final String description;
    private final String status;
    private final long menuItemCount;
}