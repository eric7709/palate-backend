package com.app.palate.utils;

import java.time.*;

public final class DateTimeUtils {

    private static final ZoneId NIGERIA_ZONE = ZoneId.of("Africa/Lagos");

    private DateTimeUtils() {
        // prevent instantiation
    }

    /** Start of day in Nigeria time → UTC Instant */
    public static Instant startOfDay(LocalDate date) {
        if (date == null) {
            date = LocalDate.now(NIGERIA_ZONE);
        }
        return date.atStartOfDay(NIGERIA_ZONE).toInstant();
    }

    /** End of day in Nigeria time → UTC Instant */
    public static Instant endOfDay(LocalDate date) {
        if (date == null) {
            date = LocalDate.now(NIGERIA_ZONE);
        }
        return date.atTime(23, 59, 59, 999_999_999).atZone(NIGERIA_ZONE).toInstant();
    }

    /** Convert LocalDateTime (assumed Nigeria time) → UTC Instant */
    public static Instant toInstant(LocalDateTime dateTime) {
        if (dateTime == null)
            return null;

        return dateTime.atZone(NIGERIA_ZONE).toInstant();
    }

    /** Convert UTC Instant → Nigeria LocalDateTime (for UI) */
    public static LocalDateTime toNigeriaTime(Instant instant) {
        if (instant == null)
            return null;

        return LocalDateTime.ofInstant(instant, NIGERIA_ZONE);
    }

    /** Start of today in Nigeria time → UTC Instant */
    public static Instant startOfToday() {
        return startOfDay(null);
    }

    /** End of today in Nigeria time → UTC Instant */
    public static Instant endOfToday() {
        return endOfDay(null);
    }
}
