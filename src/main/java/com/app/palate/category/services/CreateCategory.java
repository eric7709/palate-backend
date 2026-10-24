package com.app.palate.category.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.category.Category;
import com.app.palate.category.CategoryRepository;
import com.app.palate.category.dto.CategoryRequestDTO;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCategory {
    private final CategoryRepository categoryRepository;

    @Transactional
    public Category create(CategoryRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getName(), "Category name");

        ensureNameNotTaken(request.getName());

        Category category = new Category();
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");

        return categoryRepository.save(category);
    }

    private void ensureNameNotTaken(String name) {
        if (categoryRepository.existsByNameIgnoreCase(name.trim())) {
            throw new BadRequestException("Category with this name already exists");
        }
    }
}
