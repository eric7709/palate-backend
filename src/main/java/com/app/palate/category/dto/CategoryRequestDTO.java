package com.app.palate.category.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class CategoryRequestDTO {
    private final String name;
    private final String description;
}
