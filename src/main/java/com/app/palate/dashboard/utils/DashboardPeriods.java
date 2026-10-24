package com.app.palate.dashboard.utils;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;

import com.app.palate.utils.DashboardPeriod;

/** Works out where a dashboard period starts, and where the one before it started. */
public final class DashboardPeriods {
    private DashboardPeriods() {
    }

    /** Start of the period that contains {@code now}, in {@code now}'s time zone. */
    public static ZonedDateTime startOf(DashboardPeriod period, ZonedDateTime now) {
        return switch (period) {
            case TODAY -> now.toLocalDate().atStartOfDay(now.getZone());
            case THIS_WEEK -> now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay(now.getZone());
            case THIS_MONTH -> now.withDayOfMonth(1).toLocalDate().atStartOfDay(now.getZone());
            case THIS_YEAR -> now.withDayOfYear(1).toLocalDate().atStartOfDay(now.getZone());
        };
    }

    /** Start of the period just before the one that starts at {@code currentStart}. */
    public static ZonedDateTime previousStartOf(DashboardPeriod period, ZonedDateTime currentStart) {
        return switch (period) {
            case TODAY -> currentStart.minusDays(1);
            case THIS_WEEK -> currentStart.minusWeeks(1);
            case THIS_MONTH -> currentStart.minusMonths(1);
            case THIS_YEAR -> currentStart.minusYears(1);
        };
    }
}
