package com.app.palate.order;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.utils.DateTimeUtils;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CreateOrder createOrder;
    private final OrderEvents orderEvents;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "updatedAt", "total", "quantity");

    // ==========================
    // CREATE ORDER
    // ==========================
    public Order createOrder(OrderRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");

        // Let CreateOrder focus purely on database entity orchestration
        OrderResponseDTO responseDto = createOrder.createOrder(request, orderEvents);

        // Fetch back the created entity to keep the service signature clean
        return orderRepository.findById(responseDto.getId())
                .orElseThrow(() -> new BadRequestException("Order creation failed unexpectedly"));
    }

    // ==========================
    // UPDATE ORDER
    // ==========================
    public Order updateOrder(Long id, UpdateOrderStatusDTO request) {
        ValidationUtils.requireNonNull(id, "Order ID");
        ValidationUtils.requireNonNull(request, "Request body");

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Order not found"));

        if (request.status() != null) {
            order.setStatus(request.status());
        }
        Order saved = orderRepository.save(order);

        // Handle websocket events
        OrderResponseDTO response = OrderResponseDTO.mapToResponse(saved);
        orderEvents.broadcastUpdated(response);

        return saved;
    }

    // ==========================
    // GET ORDER BY ID
    // ==========================
    public Order getOrderById(Long id) {
        ValidationUtils.requireNonNull(id, "Order ID");

        return orderRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Order not found"));
    }

    // ==========================
    // GET ALL ORDERS (SPECIFICATION)
    // ==========================
    public OrderPageResponse getAllOrders(OrderFilterDTO filter) {
        System.out.println("DEBUGGING ORDER FILTERS: " + filter.toString());
        ValidationUtils.requireNonNull(filter, "Filter parameters");

        // 1️⃣ Sorting and Pagination
        String sortBy = filter.sortBy();
        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "createdAt";
        }

        Sort sort = "asc".equalsIgnoreCase(filter.direction())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        int page = filter.page() != null ? filter.page() : 0;
        int size = filter.size() != null ? filter.size() : 30;
        Pageable pageable = PageRequest.of(page, size, sort);

        // 2️⃣ Date handling (Lagos)
        ZoneId lagos = ZoneId.of("Africa/Lagos");

        Instant startInstant = filter.startDate() != null
                ? filter.startDate().atStartOfDay(lagos).toInstant()
                : null;
        Instant endInstant = filter.endDate() != null
                ? filter.endDate().plusDays(1).atStartOfDay(lagos).toInstant()
                : null;

        Specification<Order> listSpec = OrderSpecification.filter(
                filter.status(),
                filter.waiterId(),
                filter.cashierId(),
                filter.tableId(),
                filter.minTotal(),
                filter.maxTotal(),
                startInstant,
                endInstant,
                filter.search());

        Specification<Order> countSpec = OrderSpecification.filter(
                null, null, filter.cashierId(), null, null, null,
                startInstant, endInstant, filter.search());

        validateSpecifications(listSpec, countSpec);

        Page<Order> ordersPage = orderRepository.findAll(listSpec, pageable);

        List<Order> ordersForCounts = orderRepository.findAll(countSpec);
        OrderStatusCounts statusCounts = buildStatusCountsFromEntities(ordersForCounts);

        return new OrderPageResponse(ordersPage.map(OrderResponseDTO::mapToResponse), statusCounts);
    }

    // ==========================
    // CUSTOMER ORDERS TODAY
    // ==========================
    public List<Order> fetchCustomerOrdersToday(Long customerId) {
        ValidationUtils.requireNonNull(customerId, "Customer ID");
        Instant start = DateTimeUtils.startOfToday();
        Instant end = DateTimeUtils.endOfToday();
        return orderRepository.fetchCustomerOrdersToday(customerId, start, end);
    }

    // ==========================
    // Private Validation & Core Helpers
    // ==========================
    private void validateSpecifications(Specification<Order> listSpec, Specification<Order> countSpec) {
        if (listSpec == null || countSpec == null) {
            throw new BadRequestException("Order specifications filters could not be generated");
        }
    }

    private OrderStatusCounts buildStatusCountsFromEntities(List<Order> orders) {
        long pending = 0;
        long preparing = 0;
        long completed = 0;
        long paid = 0;
        long cancelled = 0;
        double paidTotal = 0;

        for (Order order : orders) {
            if (order.getStatus() == null)
                continue;
            if (order.getStatus().equals(OrderStatus.PENDING))
                pending++;
            if (order.getStatus().equals(OrderStatus.PREPARING))
                preparing++;
            if (order.getStatus().equals(OrderStatus.COMPLETED))
                completed++;
            if (order.getStatus().equals(OrderStatus.PAID))
                paid++;
            else
                cancelled++;
        }
        return new OrderStatusCounts(pending, preparing, completed, paid, cancelled, paidTotal);
    }
}