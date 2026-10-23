package com.app.palate.order.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.app.palate.order.OrderRepository;
import com.app.palate.order.enums.OrderStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetActiveOrdersCount {

    private final OrderRepository orderRepository;

    public long getCount() {

        return orderRepository.countByStatusNotIn(
                List.of(
                        OrderStatus.CANCELLED,
                        OrderStatus.PAID
                )
        );
    }
}