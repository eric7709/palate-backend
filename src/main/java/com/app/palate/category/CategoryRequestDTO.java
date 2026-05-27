package com.app.palate.category;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

@Getter
@RequiredArgsConstructor
public final class CategoryRequestDTO {
    private final String name;
    private final String description;
}