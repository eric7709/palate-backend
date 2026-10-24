package com.app.palate.category.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.category.Category;
import com.app.palate.category.CategoryRepository;
import com.app.palate.category.dto.CategoryRequestDTO;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Creates many categories at once. If any one is invalid or already exists, none are saved. */
@Service
@RequiredArgsConstructor
public class CreateCategoriesBulk {
    private final CategoryRepository categoryRepository;

    @Transactional
    @SuppressWarnings("null")
    public List<Category> create(List<CategoryRequestDTO> requests) {
        ValidationUtils.requireNotEmpty(requests, "Request list");

        Set<String> requestNamesLower = new HashSet<>();
        for (CategoryRequestDTO req : requests) {
            ValidationUtils.requireNonNull(req, "Category request item");
            ValidationUtils.requireNonBlank(req.getName(), "Category name");

            String normalized = req.getName().trim().toLowerCase();
            if (!requestNamesLower.add(normalized)) {
                throw new BadRequestException("Duplicate category name in request: " + req.getName().trim());
            }
        }

        List<String> existingNames = categoryRepository.findExistingNamesIgnoreCase(new ArrayList<>(requestNamesLower));
        if (!existingNames.isEmpty()) {
            throw new BadRequestException("Categories already exist: " + String.join(", ", existingNames));
        }

        List<Category> categories = requests.stream()
                .map(req -> {
                    Category cat = new Category();
                    cat.setName(req.getName().trim());
                    cat.setDescription(req.getDescription() != null ? req.getDescription().trim() : "");
                    return cat;
                })
                .toList();

        return categoryRepository.saveAll(categories);
    }
}
