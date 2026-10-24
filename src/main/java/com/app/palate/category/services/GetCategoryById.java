package com.app.palate.category.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.category.Category;
import com.app.palate.category.CategoryRepository;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCategoryById {
    private final CategoryRepository categoryRepository;

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public Category get(Long id) {
        ValidationUtils.requireNonNull(id, "Category ID");

        return categoryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Category not found"));
    }
}
