package com.app.palate.order.dto;

import com.app.palate.order.enums.OrderStatus;

public record UpdateOrderStatusDTO(
    OrderStatus status
) {}
