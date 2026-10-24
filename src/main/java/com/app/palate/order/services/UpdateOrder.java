package com.app.palate.order.services;

import org.springframework.stereotype.Service;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.order.Order;
import com.app.palate.order.OrderRepository;
import com.app.palate.order.dto.OrderResponseDTO;
import com.app.palate.order.dto.UpdateOrderStatusDTO;
import com.app.palate.order.helpers.OrderEvents;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateOrder {

    private final OrderRepository orderRepository;
    private final OrderEvents orderEvents;

    @SuppressWarnings("null")
    public Order execute(Long id, UpdateOrderStatusDTO request) {

        ValidationUtils.requireNonNull(id, "Order ID");
        ValidationUtils.requireNonNull(request, "Request body");

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException("Order not found"));

        if (request.status() != null) {
            order.setStatus(request.status());
        }

        Order saved = orderRepository.save(order);

        OrderResponseDTO response =
                OrderResponseDTO.mapToResponse(saved);

        orderEvents.broadcastUpdated(response);

        return saved;
    }
}