package com.app.palate.order;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/palate/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDTO createOrder(@RequestBody OrderRequestDTO request) {
        Order order = orderService.createOrder(request);
        return OrderResponseDTO.mapToResponse(order);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public OrderPageResponse getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long waiterId,
            @RequestParam(required = false) Long cashierId,
            @RequestParam(required = false) Long tableId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minTotal,
            @RequestParam(required = false) Double maxTotal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        OrderFilterDTO filter = new OrderFilterDTO(
                status, waiterId, cashierId, tableId, search,
                minTotal, maxTotal, startDate, endDate, page, size, sortBy, sortDirection);
        return orderService.getAllOrders(filter);
    }

    
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponseDTO updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusDTO request) {
        Order order = orderService.updateOrder(id, request);
        return OrderResponseDTO.mapToResponse(order);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponseDTO getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return OrderResponseDTO.mapToResponse(order);
    }

    @GetMapping("/customer/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerOrderDTO> fetchCustomerOrdersToday(@PathVariable Long id) {
        List<Order> orders = orderService.fetchCustomerOrdersToday(id);
        return CustomerOrderDTO.mapToResponse(orders);
    }
}