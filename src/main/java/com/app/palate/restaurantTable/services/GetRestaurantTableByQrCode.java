package com.app.palate.restaurantTable.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Finds the table a customer scanned the QR code of. */
@Service
@RequiredArgsConstructor
public class GetRestaurantTableByQrCode {
    private final RestaurantTableRepository restaurantTableRepository;

    @Transactional(readOnly = true)
    public RestaurantTable get(String qrCode) {
        ValidationUtils.requireNonBlank(qrCode, "QR Code token");

        return restaurantTableRepository.findByQrCode(qrCode.trim())
                .orElseThrow(() -> new BadRequestException("Invalid or unrecognized QR code token"));
    }
}
