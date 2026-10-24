package com.app.palate.menuItem.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.menuItem.MenuItem;
import com.app.palate.menuItem.MenuItemRepository;
import com.app.palate.menuItem.enums.MenuItemStatus;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Of the menu item ids given, returns the ones that are currently AVAILABLE. */
@Service
@RequiredArgsConstructor
public class GetAvailableMenuItems {
    private final MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public List<Long> get(List<Long> menuItemIds) {
        ValidationUtils.requireNotEmpty(menuItemIds, "Menu item ID list");
        List<Long> availableMenuItems = new ArrayList<>();

        for (Long menuItemId : menuItemIds) {
            ValidationUtils.requireNonNull(menuItemId, "Menu item ID element");
            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new BadRequestException("Menu item not found"));

            if (MenuItemStatus.AVAILABLE.equals(menuItem.getStatus())) {
                availableMenuItems.add(menuItemId);
            }
        }
        return availableMenuItems;
    }
}
