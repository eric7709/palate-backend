package com.app.palate.order.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.app.palate.order.enums.OrderStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderSearchCriteria {
    private OrderStatus status;
    private Long waiterId;
    private Long cashierId;
    private Long tableId;
    private Long roomId;
    private String search;
    private Double minTotal;
    private Double maxTotal;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";

    public OrderFilterDTO toFilter() {
        return new OrderFilterDTO(
                status, waiterId, cashierId, tableId, roomId, search,
                minTotal, maxTotal, startDate, endDate, page, size, sortBy, sortDirection);
    }
}
