package com.app.palate.room.dto;

import com.app.palate.room.enums.RoomStatus;

import lombok.Data;

@Data
public class RoomResponseDTO {
    private Long id;
    private String roomNumber;
    private Integer floor;
    private String qrCode;
    private RoomStatus status;
    private Long cashierId;
    private String cashierName;
}
