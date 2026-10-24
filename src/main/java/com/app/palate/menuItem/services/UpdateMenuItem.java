package com.app.palate.menuItem.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.category.Category;
import com.app.palate.category.CategoryRepository;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.menuItem.MenuItem;
import com.app.palate.menuItem.MenuItemEvents;
import com.app.palate.menuItem.MenuItemRepository;
import com.app.palate.menuItem.dto.MenuItemRequestDTO;
import com.app.palate.menuItem.dto.MenuItemResponseDTO;
import com.app.palate.menuItem.utils.MenuItemMapper;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Changes only the fields that are present in the request; anything left out stays as it was. */
@Service
@RequiredArgsConstructor
public class UpdateMenuItem {
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemEvents menuItemEvents;
    private final MenuItemMapper menuItemMapper;

    @Transactional
    @SuppressWarnings("null")
    public MenuItem update(Long id, MenuItemRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Menu Item ID");

        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Menu Item not found"));

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new BadRequestException("Category not found"));
            menuItem.setCategory(category);
        }

        if (request.name() != null && !request.name().isBlank()) {
            ensureNameNotTakenByAnother(request.name(), id);
            menuItem.setName(request.name().trim());
        }

        if (request.description() != null && !request.description().isBlank()) {
            menuItem.setDescription(request.description().trim());
        }

        if (request.price() != null) {
            ValidationUtils.requireGreaterThanZero(request.price(), "Price");
            menuItem.setPrice(request.price());
        }

        if (request.imageUrl() != null) {
            menuItem.setImageUrl(request.imageUrl());
        }

        if (request.status() != null) {
            menuItem.setStatus(request.status());
        }

        MenuItemResponseDTO response = menuItemMapper.toResponse(menuItem);
        menuItemEvents.broadcastCreated(response);
        return menuItemRepository.save(menuItem);
    }

    private void ensureNameNotTakenByAnother(String name, Long id) {
        if (menuItemRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), id)) {
            throw new BadRequestException("Another menu item with this name already exists");
        }
    }
}
