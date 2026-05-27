package com.app.palate.menuItem;

public record MenuItemRequestDTO(String name, Long categoryId, Double price, MenuItemStatus status, String imageUrl, String description) {

}


