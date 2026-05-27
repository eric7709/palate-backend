package com.app.palate.orderItem;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

@Getter
@RequiredArgsConstructor
public final class OrderItemResponse {
    private final Long id;
    private final String menuItemName;
    private final Long menuItemId;
    private final int quantity;
    private final double price;
    private final boolean takeOut;
}