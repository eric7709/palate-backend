package com.app.palate.order.services;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.app.palate.order.Order;
import com.app.palate.order.OrderRepository;
import com.app.palate.utils.DateTimeUtils;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCustomerOrdersToday {

    private final OrderRepository orderRepository;

    public List<Order> get(Long customerId) {
        ValidationUtils.requireNonNull(customerId, "Customer ID");

        Instant start = DateTimeUtils.startOfToday();
        Instant end = DateTimeUtils.endOfToday();

        return orderRepository.findByCustomerIdAndCreatedAtBetween(customerId, start, end);
    }
}