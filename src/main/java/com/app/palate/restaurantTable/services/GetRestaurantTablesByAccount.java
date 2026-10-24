package com.app.palate.restaurantTable.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;

import lombok.RequiredArgsConstructor;

/** The tables looked after by a waiter, or, if no waiter is given, by a cashier. */
@Service
@RequiredArgsConstructor
public class GetRestaurantTablesByAccount {
    private final RestaurantTableRepository restaurantTableRepository;

    @Transactional(readOnly = true)
    public List<RestaurantTable> get(Long waiterId, Long cashierId) {
        if (waiterId != null) return restaurantTableRepository.findByWaiterId(waiterId);
        if (cashierId != null) return restaurantTableRepository.findByCashierId(cashierId);
        return null;
    }
}
