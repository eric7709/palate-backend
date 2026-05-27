package com.app.palate.order;

import java.time.LocalDate;
// OrderFilterDTO.java
public record OrderFilterDTO(
    OrderStatus status,
    Long waiterId,
    Long cashierId,
    Long tableId,
    String search,          // new: general search (invoice, customer name, waiter/cashier username)
    Double minTotal,
    Double maxTotal,
    LocalDate startDate,
    LocalDate endDate,
    Integer page,
    Integer size,
    String sortBy,
    String direction
) {}