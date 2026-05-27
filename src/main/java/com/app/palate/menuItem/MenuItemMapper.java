package com.app.palate.menuItem;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    MenuItemResponseDTO toResponse(MenuItem item);

    // MapStruct automatically handles list iterations for you!
    List<MenuItemResponseDTO> mapAllToResponse(List<MenuItem> menuItems);
    
}