package com.app.palate.analytics.utils;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Safe conversions for the loosely-typed values that native and aggregate queries return. */
public final class RowValues {
    private RowValues() {
    }

    public static double nullSafe(Double val) {
        return val != null ? val : 0.0;
    }

    public static long nullSafe(Long val) {
        return val != null ? val : 0L;
    }

    public static long toLong(Object val) {
        if (val == null)
            return 0L;
        if (val instanceof Long l)
            return l;
        if (val instanceof Number n)
            return n.longValue();
        return Long.parseLong(val.toString());
    }

    public static double toDouble(Object val) {
        if (val == null)
            return 0.0;
        if (val instanceof Double d)
            return d;
        if (val instanceof BigDecimal b)
            return b.doubleValue();
        if (val instanceof Number n)
            return n.doubleValue();
        return Double.parseDouble(val.toString());
    }

    public static int toInt(Object val) {
        if (val == null)
            return 0;
        if (val instanceof Integer i)
            return i;
        if (val instanceof Number n)
            return n.intValue();
        return Integer.parseInt(val.toString());
    }

    public static LocalDate toLocalDate(Object val) {
        if (val instanceof java.sql.Date d)
            return d.toLocalDate();
        if (val instanceof LocalDate ld)
            return ld;
        return LocalDate.parse(val.toString());
    }
}
