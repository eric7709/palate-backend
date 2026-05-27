package com.app.palate.auth;

public record LoginRequestDTO(
        String email,
        String password
) {}