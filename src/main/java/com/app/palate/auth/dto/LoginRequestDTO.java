package com.app.palate.auth.dto;

public record LoginRequestDTO(
        String email,
        String password
) {}
