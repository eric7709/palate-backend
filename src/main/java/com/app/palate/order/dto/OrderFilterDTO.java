package com.app.palate.order.dto;

import java.time.LocalDate;

import com.app.palate.order.enums.OrderStatus;

public record OrderFilterDTO(
    OrderStatus status,
    Long waiterId,
    Long cashierId,
    Long tableId,
    Long roomId,
    String search,      // invoice number, customer name, waiter/cashier username
    Double minTotal,
    Double maxTotal,
    LocalDate startDate,
    LocalDate endDate,
    Integer page,
    Integer size,
    String sortBy,
    String direction
) {}