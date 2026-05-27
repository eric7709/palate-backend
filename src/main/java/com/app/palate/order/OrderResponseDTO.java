package com.app.palate.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.app.palate.orderItem.OrderItem;
import com.app.palate.orderItem.OrderItemResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private String invoiceNumber;
    private OrderStatus orderStatus;
    private Integer quantity;
    private double total;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Nested Grouped Data
    private UserSummaryDTO waiter;
    private UserSummaryDTO cashier;
    private CustomerSummaryDTO customer;
    private TableSummaryDTO table;
    private List<OrderItemResponse> items;

    // --- NESTED SUB-DTOs ---
    
    @Getter
@Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSummaryDTO {
        private Long id;
        private String fullName;
    }

    @Getter
@Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CustomerSummaryDTO {
        private Long id;
        private String name;
        private String title;
    }

    @Getter
@Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TableSummaryDTO {
        private Long id;
        private Integer tableNumber;
        private String tableName;
    }

    // --- MAPPING LOGIC ---

    public static List<OrderItemResponse> mapToOrderItem(List<OrderItem> items) {
        List<OrderItemResponse> response = new ArrayList<>();
        if (items == null) return response;
        
        for (OrderItem item : items) {
            response.add(new OrderItemResponse(
                    item.getId(),
                    item.getMenuItem() != null ? item.getMenuItem().getName() : null,
                    item.getMenuItem() != null ? item.getMenuItem().getId() : null,
                    item.getQuantity(),
                    item.getPrice(),
                    item.isTakeOut()
            ));
        }
        return response;
    }

    public static OrderResponseDTO mapToResponse(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();

        dto.setId(order.getId());
        dto.setInvoiceNumber(order.getInvoiceNumber());
        dto.setOrderStatus(order.getStatus());
        dto.setQuantity(order.getQuantity());
        dto.setTotal(order.getTotal());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setItems(mapToOrderItem(order.getItems()));

        // Map Customer Group
        if (order.getCustomer() != null) {
            dto.setCustomer(new CustomerSummaryDTO(
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getCustomer().getTitle()
            ));
        }

        // Map Table Group
        if (order.getTable() != null) {
            dto.setTable(new TableSummaryDTO(
                order.getTable().getId(),
                order.getTable().getTableNumber(),
                order.getTable().getTableName()
            ));
        }

        // Map Waiter Group
        if (order.getWaiter() != null) {
            String waiterFullName = order.getWaiter().getFirstName() + " " + order.getWaiter().getLastName();
            dto.setWaiter(new UserSummaryDTO(order.getWaiter().getId(), waiterFullName.trim()));
        }

        // Map Cashier Group
        if (order.getCashier() != null) {
            String cashierFullName = order.getCashier().getFirstName() + " " + order.getCashier().getLastName();
            dto.setCashier(new UserSummaryDTO(order.getCashier().getId(), cashierFullName.trim()));
        }

        return dto;
    }
}