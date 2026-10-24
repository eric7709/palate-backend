package com.app.palate.room.dto;

import com.app.palate.room.enums.RoomStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Query parameters for listing rooms: what to look for, which filters, which page, and how to sort. */
@Getter
@Setter
@NoArgsConstructor
public class RoomSearchCriteria {
    private String search;
    private RoomStatus status;
    private Integer floor;
    private Long cashierId;
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}
