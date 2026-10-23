package com.app.palate.room;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
