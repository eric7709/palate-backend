package com.app.palate.order.services;

import com.app.palate.order.Order;
import com.app.palate.order.OrderRepository;
import com.app.palate.order.dto.OrderResponseDTO;
import com.app.palate.order.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPreparingOrders {
    private final OrderRepository orderRepository;

    public List<OrderResponseDTO> get() {
        ZoneId zone = ZoneId.of("Africa/Lagos");
        LocalDate today = LocalDate.now(zone);
        Instant startOfDay = today.atStartOfDay(zone).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant();
        List<Order> orders = orderRepository.findByStatusAndCreatedAtBetween(
                OrderStatus.PREPARING, startOfDay, endOfDay);
        return orders.stream()
                .map(OrderResponseDTO::mapToResponse)
                .collect(Collectors.toList());
    }
}