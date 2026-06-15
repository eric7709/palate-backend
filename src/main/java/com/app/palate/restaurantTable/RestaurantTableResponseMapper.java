package com.app.palate.restaurantTable;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface RestaurantTableResponseMapper {

    @Mapping(target = "waiterId", source = "waiter.id")
    @Mapping(target = "waiterName", source = "waiter", qualifiedByName = "mapFullName")
    @Mapping(target = "cashierId", source = "cashier.id")
    @Mapping(target = "cashierName", source = "cashier", qualifiedByName = "mapFullName")
    RestaurantTableResponseDTO mapToResponse(RestaurantTable table);

    @Named("mapFullName")
    default String mapFullName(com.app.palate.auth.Account account) {
        if (account == null) {
            return null;
        }
        String firstName = account.getFirstName() != null ? account.getFirstName() : "";
        String lastName = account.getLastName() != null ? account.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }
}