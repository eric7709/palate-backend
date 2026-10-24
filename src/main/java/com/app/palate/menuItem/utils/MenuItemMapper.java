package com.app.palate.menuItem.utils;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.app.palate.menuItem.MenuItem;
import com.app.palate.menuItem.dto.MenuItemResponseDTO;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    MenuItemResponseDTO toResponse(MenuItem item);

    List<MenuItemResponseDTO> mapAllToResponse(List<MenuItem> menuItems);
}
