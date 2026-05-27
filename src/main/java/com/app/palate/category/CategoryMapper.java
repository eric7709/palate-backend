package com.app.palate.category;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    @Mapping(target = "menuItemCount", source = "menuItems", qualifiedByName = "mapMenuItemsToCount")
    @Mapping(target = "description", source = "description", defaultValue = "")
    CategoryResponseDTO mapToDto(Category category);

    @Named("mapMenuItemsToCount")
    default long mapMenuItemsToCount(Collection<?> menuItems) {
        return menuItems == null ? 0 : menuItems.size();
    }
}