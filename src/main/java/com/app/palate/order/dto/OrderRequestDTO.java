package com.app.palate.order.dto;

import java.util.List;

import com.app.palate.order.enums.OrderStatus;
import com.app.palate.orderItem.OrderItemDTO;
import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRequestDTO {

    private Long tableId;
    private Long roomId;

    private Long waiterId;
    private Long cashierId;

    @JsonAlias("orderStatus")
    private OrderStatus status;

    private List<OrderItemDTO> items;

    private Long customerId;
    private String note;
    private String customerName;
    private String customerPhoneNumber;
    private String customerTitle;
}