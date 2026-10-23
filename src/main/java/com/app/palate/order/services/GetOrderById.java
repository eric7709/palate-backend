package com.app.palate.order.services;

import org.springframework.stereotype.Service;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.order.Order;
import com.app.palate.order.OrderRepository;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetOrderById {

    private final OrderRepository orderRepository;

    public Order get(Long id) {

        ValidationUtils.requireNonNull(id, "Order ID");

        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException("Order not found"));
    }
}