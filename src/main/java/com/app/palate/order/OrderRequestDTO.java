package com.app.palate.order;

import java.util.List;

import com.app.palate.orderItem.OrderItemDTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class OrderRequestDTO {

    private final Long tableId;
    private final Long roomId;

    private final Long waiterId;
    private final Long cashierId;

    private final OrderStatus status;

    private final List<OrderItemDTO> items;

    private final Long customerId;
    private final String note;
    private final String customerName;
    private final String customerPhoneNumber;
    private final String customerTitle;
}