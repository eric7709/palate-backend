package com.app.palate.menuItem.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.menuItem.MenuItem;
import com.app.palate.menuItem.MenuItemRepository;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMenuItemById {
    private final MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public MenuItem get(Long id) {
        ValidationUtils.requireNonNull(id, "Menu Item ID");

        return menuItemRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Menu item not found"));
    }
}
