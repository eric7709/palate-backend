package com.app.palate.room;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "cashierId", source = "cashier.id")
    @Mapping(
        target = "cashierName",
        expression = """
            java(room.getCashier() != null
                ? room.getCashier().getFirstName() + " " + room.getCashier().getLastName()
                : null)
            """
    )
    RoomResponseDTO toDTO(Room room);
}