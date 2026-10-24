package com.app.palate.category.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.category.CategoryRepository;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteCategory {
    private final CategoryRepository categoryRepository;

    @Transactional
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Category ID");

        if (!categoryRepository.existsById(id)) {
            throw new BadRequestException("Category not found");
        }
        categoryRepository.deleteById(id);
    }
}
