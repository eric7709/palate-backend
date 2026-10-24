package com.app.palate.restaurantTable.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetRestaurantTableById {
    private final RestaurantTableRepository restaurantTableRepository;

    @Transactional(readOnly = true)
    public RestaurantTable get(Long id) {
        ValidationUtils.requireNonNull(id, "Table ID");

        return restaurantTableRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Table not found"));
    }
}
