package com.app.palate.room.utils;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.app.palate.room.Room;
import com.app.palate.room.dto.RoomResponseDTO;

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
