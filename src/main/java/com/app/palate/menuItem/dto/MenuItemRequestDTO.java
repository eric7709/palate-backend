package com.app.palate.menuItem.dto;

import com.app.palate.menuItem.enums.MenuItemStatus;

public record MenuItemRequestDTO(String name, Long categoryId, Double price, MenuItemStatus status, String imageUrl, String description) {

}
