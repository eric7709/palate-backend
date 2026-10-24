package com.app.palate.restaurantTable.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetRestaurantTableById {
    private final RestaurantTableRepository restaurantTableRepository;

    @Transactional(readOnly = true)
    public RestaurantTable get(Long id) {

        if(id  == null){
            throw new BadRequestException("Table id is required");
        }

        return restaurantTableRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Table not found"));
    }
}
