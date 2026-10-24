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
public class UpdateCategory {
    private final CategoryRepository categoryRepository;

    @Transactional
    @SuppressWarnings("null")
    public Category update(Long id, CategoryRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Category ID");
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getName(), "Category name");

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Category not found"));

        ensureNameNotTakenByAnother(request.getName(), id);

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");

        return categoryRepository.save(category);
    }

    private void ensureNameNotTakenByAnother(String name, Long id) {
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), id)) {
            throw new BadRequestException("Another category with this name already exists");
        }
    }
}
