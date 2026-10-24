package com.app.palate.menuItem.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.menuItem.MenuItemRepository;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteMenuItem {
    private final MenuItemRepository menuItemRepository;

    @Transactional
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Menu Item ID");

        if (!menuItemRepository.existsById(id)) {
            throw new BadRequestException("Menu item not found");
        }
        menuItemRepository.deleteById(id);
    }
}
