package com.app.palate.utils;

import java.util.Collection;
import java.util.Map;
import com.app.palate.exceptions.BadRequestException;

public final class ValidationUtils {

    private ValidationUtils() {
        // Prevent instantiation of utility class
    }

    // ---------- Object Validation ----------
    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new BadRequestException(fieldName + " is required");
        }
    }

    // ---------- String Validation ----------
    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " cannot be empty or blank");
        }
    }

    // ---------- Number Validation ----------
    public static void requireGreaterThanZero(Number value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.doubleValue() <= 0) {
            throw new BadRequestException(fieldName + " must be greater than zero");
        }
    }

    public static void requirePositive(Number value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.doubleValue() < 0) {
            throw new BadRequestException(fieldName + " cannot be negative");
        }
    }

    // ---------- Collections & Arrays Validation ----------
    public static void requireNotEmpty(Collection<?> collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            throw new BadRequestException(fieldName + " must contain at least one item");
        }
    }

    public static void requireNotEmpty(Object[] array, String fieldName) {
        if (array == null || array.length == 0) {
            throw new BadRequestException(fieldName + " must contain at least one item");
        }
    }
    
    public static void requireNotEmpty(Map<?, ?> map, String fieldName) {
        if (map == null || map.isEmpty()) {
            throw new BadRequestException(fieldName + " must not be empty");
        }
    }
}