package com.app.palate.room.dto;

import com.app.palate.room.enums.RoomStatus;

import lombok.Data;

@Data
public class RoomRequestDTO {
    private String roomNumber;
    private Integer floor;
    private RoomStatus status;
    private Long cashierId;
}
