package com.app.palate.menuItem.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.category.Category;
import com.app.palate.category.CategoryRepository;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.menuItem.MenuItem;
import com.app.palate.menuItem.MenuItemRepository;
import com.app.palate.menuItem.dto.MenuItemRequestDTO;
import com.app.palate.menuItem.enums.MenuItemStatus;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Creates many menu items at once. If any one is invalid or already exists, none are saved. */
@Service
@RequiredArgsConstructor
public class CreateMenuItemsBulk {
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional
    @SuppressWarnings("null")
    public List<MenuItem> create(List<MenuItemRequestDTO> requests) {
        ValidationUtils.requireNotEmpty(requests, "Bulk request list");

        List<MenuItem> menuItemsToSave = new ArrayList<>();
        Set<String> incomingNamesLower = new HashSet<>();
        Map<Long, Category> categoryCache = new HashMap<>();

        for (MenuItemRequestDTO req : requests) {
            ValidationUtils.requireNonNull(req, "Bulk item");
            ValidationUtils.requireNonBlank(req.name(), "Menu item name");
            ValidationUtils.requireNonBlank(req.description(), "Description");
            ValidationUtils.requireNonNull(req.categoryId(), "Category ID");
            ValidationUtils.requireGreaterThanZero(req.price(), "Price");

            String normalizedName = req.name().trim().toLowerCase();
            if (!incomingNamesLower.add(normalizedName)) {
                throw new BadRequestException("Duplicate menu item name within request: " + req.name().trim());
            }
        }

        for (MenuItemRequestDTO req : requests) {
            ensureNameNotTaken(req.name());

            Category category = categoryCache.computeIfAbsent(req.categoryId(), id -> categoryRepository.findById(id)
                    .orElseThrow(() -> new BadRequestException("Category ID " + id + " not found")));

            MenuItemStatus status = req.status() == null ? MenuItemStatus.AVAILABLE : req.status();
            String imageUrl = (req.imageUrl() != null && !req.imageUrl().isBlank())
                    ? req.imageUrl().trim()
                    : null;

            MenuItem menuItem = new MenuItem();
            menuItem.setName(req.name().trim());
            menuItem.setDescription(req.description().trim());
            menuItem.setCategory(category);
            menuItem.setPrice(req.price());
            menuItem.setStatus(status);
            menuItem.setImageUrl(imageUrl);

            menuItemsToSave.add(menuItem);
        }

        return menuItemRepository.saveAll(menuItemsToSave);
    }

    private void ensureNameNotTaken(String name) {
        if (menuItemRepository.existsByNameIgnoreCase(name.trim())) {
            throw new BadRequestException("Menu item with this name already exists");
        }
    }
}
