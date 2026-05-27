package com.app.palate.orderItem;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

@Getter
@RequiredArgsConstructor
public final class OrderItemDTO {
    private final Long menuItemId;
    private final int quantity;
    private final boolean takeOut;
}