package com.app.palate.order;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.order.dto.CustomerOrderDTO;
import com.app.palate.order.dto.OrderPageResponse;
import com.app.palate.order.dto.OrderRequestDTO;
import com.app.palate.order.dto.OrderSearchCriteria;

import com.app.palate.order.dto.OrderResponseDTO;
import com.app.palate.order.dto.OrderSummaryResponse;
import com.app.palate.order.dto.UpdateOrderStatusDTO;
import com.app.palate.order.enums.OrderStatus;
import com.app.palate.order.helpers.OrderEvents;
import com.app.palate.order.services.CreateOrder;

import com.app.palate.order.services.GetActiveOrdersCount;
import com.app.palate.order.services.GetAllCustomerOrders;
import com.app.palate.order.services.GetAllOrders;
import com.app.palate.order.services.GetCustomerOrdersToday;
import com.app.palate.order.services.GetOrderById;

import com.app.palate.order.services.GetOrderSummary;
import com.app.palate.order.services.GetPreparingOrders;
import com.app.palate.order.services.UpdateOrder;
import com.app.palate.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/palate/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
        private final CreateOrder createOrder;
        private final GetOrderSummary getOrderSummary;
        private final GetAllOrders getAllOrders;
        private final GetActiveOrdersCount getActiveOrdersCount;
        private final UpdateOrder updateOrder;
        private final GetOrderById getOrderById;
        private final OrderEvents orderEvents;
        private final OrderRepository orderRepository;
        private final GetCustomerOrdersToday getCustomerOrdersToday;
        private final GetAllCustomerOrders getAllCustomerOrders;
        private final GetPreparingOrders getPreparingOrders;

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public Order createOrder(OrderRequestDTO request) {
                ValidationUtils.requireNonNull(request, "Request body");
                OrderResponseDTO responseDto = createOrder.create(request, orderEvents);
                return orderRepository.findById(responseDto.getId()).orElseThrow(() -> new BadRequestException(""));
        }

        @GetMapping
        @ResponseStatus(HttpStatus.OK)
        public OrderPageResponse getAllOrders(OrderSearchCriteria criteria) {
                return getAllOrders.get(criteria.toFilter());

        }

        @GetMapping("/active/count")
        public ResponseEntity<Long> getActiveOrdersCount() {
                return ResponseEntity.ok(getActiveOrdersCount.getCount());
        }

        @PatchMapping("/{id}")
        @ResponseStatus(HttpStatus.OK)
        public OrderResponseDTO updateOrderStatus(
                        @PathVariable Long id,
                        @RequestBody UpdateOrderStatusDTO request) {

                Order order = updateOrder.update(id, request);
                return OrderResponseDTO.mapToResponse(order);
        }

        @GetMapping("/{id}")
        @ResponseStatus(HttpStatus.OK)
        public OrderResponseDTO getOrderById(@PathVariable Long id) {
                Order order = getOrderById.get(id);
                return OrderResponseDTO.mapToResponse(order);
        }

        @GetMapping("/customer/{id}")
        @ResponseStatus(HttpStatus.OK)
        public List<CustomerOrderDTO> fetchCustomerOrdersToday(@PathVariable Long id) {
                List<Order> orders = getCustomerOrdersToday.get(id);
                return CustomerOrderDTO.mapToResponse(orders);
        }

        @GetMapping("/customer/{id}/all")
        @ResponseStatus(HttpStatus.OK)
        public List<CustomerOrderDTO> fetchAllCustomerOrders(@PathVariable Long id) {
                List<Order> orders = getAllCustomerOrders.get(id);
                return CustomerOrderDTO.mapToResponse(orders);
        }

        @GetMapping("/summary")
        public ResponseEntity<OrderSummaryResponse> getSummary(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) Long waiterId,
                        @RequestParam(required = false) Long cashierId) {
                LocalDate start = startDate != null ? startDate : LocalDate.now();
                LocalDate end = endDate != null ? endDate : LocalDate.now();
                return ResponseEntity.ok(getOrderSummary.get(start, end, waiterId, cashierId));
        }

        @GetMapping("/preparing/today")
        @ResponseStatus(HttpStatus.OK)
        public List<OrderResponseDTO> getPreparingOrdersToday() {
                return getPreparingOrders.get();
        }
}