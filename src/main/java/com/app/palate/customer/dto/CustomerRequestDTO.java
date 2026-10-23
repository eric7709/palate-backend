package com.app.palate.customer.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class CustomerRequestDTO {
    private final String name;
    private final String phoneNumber;
    private final String title;
    private final String email;
}
