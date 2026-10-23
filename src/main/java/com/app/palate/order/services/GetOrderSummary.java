package com.app.palate.order.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.app.palate.order.OrderRepository;
import com.app.palate.order.dto.OrderSummaryProjection;
import com.app.palate.order.dto.OrderSummaryResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class GetOrderSummary {
    private final OrderRepository orderRepository;

    public OrderSummaryResponse get(LocalDate startDate, LocalDate endDate, Long waiterId,
            Long cashierId) {
        ZoneId zone = ZoneId.of("Africa/Lagos");
        Instant startOfDay = startDate.atStartOfDay(zone).toInstant();
        Instant endOfDay = endDate.plusDays(1).atStartOfDay(zone).toInstant();

        OrderSummaryProjection projection = orderRepository.getSummaryByDate(
                startOfDay, endOfDay, waiterId, cashierId);

        return OrderSummaryResponse.builder()
                .totalOrders(projection.getTotalOrders() != null ? projection.getTotalOrders() : 0L)
                .pending(projection.getPending() != null ? projection.getPending() : 0L)
                .completed(projection.getCompleted() != null ? projection.getCompleted() : 0L)
                .cancelled(projection.getCancelled() != null ? projection.getCancelled() : 0L)
                .preparing(projection.getPreparing() != null ? projection.getPreparing() : 0L)
                .paid(projection.getPaid() != null ? projection.getPaid() : 0L) // Add this
                .totalAmount(projection.getTotalAmount() != null ? projection.getTotalAmount() : 0.0)
                .build();
    }
}