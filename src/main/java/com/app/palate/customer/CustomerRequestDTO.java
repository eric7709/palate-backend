package com.app.palate.customer;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

@Getter
@RequiredArgsConstructor
public final class CustomerRequestDTO {
    private final String name;
    private final String phoneNumber;
    private final String title;
    private final String email;
}