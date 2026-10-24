package com.app.palate.auth.dto;

public record ChangePasswordRequestDTO(
        String currentPassword,
        String newPassword
) {}
