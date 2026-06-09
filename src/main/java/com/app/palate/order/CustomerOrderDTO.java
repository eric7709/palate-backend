package com.app.palate.order;

import java.util.List;

import com.app.palate.orderItem.OrderItemResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CustomerOrderDTO {

        private List<OrderItemResponse> items;
        private double total;
        private Integer quantity;
        private String invoiceNumber;
        private OrderStatus orderStatus;
        private String virtualAccountNumber;
        private String virtualBankName;
        private String orderDate;

        public static List<CustomerOrderDTO> mapToResponse(List<Order> orders) {
                return orders.stream()
                                .map(order -> new CustomerOrderDTO(
                                                order.getItems().stream()
                                                                .map(el -> new OrderItemResponse(
                                                                                el.getId(),
                                                                                el.getMenuItem().getName(),
                                                                                el.getMenuItem().getId(),
                                                                                el.getQuantity(),
                                                                                el.getPrice(),
                                                                                el.isTakeOut()))
                                                                .toList(),
                                                order.getTotal(),
                                                order.getQuantity(),
                                                order.getInvoiceNumber(),
                                                order.getStatus(),
                                                order.getVirtualAccountNumber(),
                                                order.getVirtualBankName(),
                                                order.getCreatedAt().toString()))
                                .toList();
        }
}
