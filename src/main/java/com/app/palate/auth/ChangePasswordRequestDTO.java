package com.app.palate.auth;

public record ChangePasswordRequestDTO(
        String currentPassword,
        String newPassword
) {}
