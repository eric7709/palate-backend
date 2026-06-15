package com.app.palate.room;

import lombok.Data;

@Data
public class RoomRequestDTO {
    private String roomNumber;
    private Integer floor;
    private RoomStatus status;
    private Long cashierId;
}