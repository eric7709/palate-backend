package com.app.palate.menuItem.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.category.Category;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.menuItem.MenuItem;
import com.app.palate.menuItem.MenuItemEvents;
import com.app.palate.menuItem.MenuItemRepository;
import com.app.palate.menuItem.dto.MenuItemRequestDTO;
import com.app.palate.menuItem.dto.MenuItemResponseDTO;
import com.app.palate.menuItem.enums.MenuItemStatus;
import com.app.palate.menuItem.utils.MenuItemMapper;
import com.app.palate.utils.EntityResolver;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateMenuItem {
    private final EntityResolver entityResolver;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemEvents menuItemEvents;
    private final MenuItemMapper menuItemMapper;

    @Transactional
    public MenuItem create(MenuItemRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");

        ValidationUtils.requireNonBlank(request.name(), "Menu item name");
        ValidationUtils.requireNonBlank(request.description(), "Description");
        ValidationUtils.requireNonNull(request.categoryId(), "Category ID");
        ValidationUtils.requireGreaterThanZero(request.price(), "Price");

        ensureNameNotTaken(request.name());

        Category category = entityResolver.resolveCategory(request.categoryId());

        MenuItemStatus status = request.status() == null ? MenuItemStatus.AVAILABLE : request.status();
        String imageUrl = (request.imageUrl() != null && !request.imageUrl().isBlank())
                ? request.imageUrl().trim()
                : null;

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.name().trim());
        menuItem.setDescription(request.description().trim());
        menuItem.setCategory(category);
        menuItem.setPrice(request.price());
        menuItem.setStatus(status);
        menuItem.setImageUrl(imageUrl);

        MenuItemResponseDTO response = menuItemMapper.toResponse(menuItem);
        menuItemEvents.broadcastCreated(response);
        return menuItemRepository.save(menuItem);
    }

    private void ensureNameNotTaken(String name) {
        if (menuItemRepository.existsByNameIgnoreCase(name.trim())) {
            throw new BadRequestException("Menu item with this name already exists");
        }
    }
}
