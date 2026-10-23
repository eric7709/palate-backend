package com.app.palate.order.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.app.palate.order.Order;
import com.app.palate.order.OrderRepository;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAllCustomerOrders {

    private final OrderRepository orderRepository;

    public List<Order> get(Long customerId) {
        ValidationUtils.requireNonNull(customerId, "Customer ID");

        return orderRepository.findByCustomerId(customerId);
    }
}