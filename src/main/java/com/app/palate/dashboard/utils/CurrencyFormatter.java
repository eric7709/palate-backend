package com.app.palate.dashboard.utils;

import java.math.BigDecimal;

/** Short naira amounts for dashboard cards, e.g. ₦950, ₦142k, ₦3M. */
public final class CurrencyFormatter {
    private CurrencyFormatter() {
    }

    public static String format(long amount) {
        if (amount >= 1_000_000)
            return "₦" + (amount / 1_000_000) + "M";
        if (amount >= 1_000)
            return "₦" + (amount / 1_000) + "k";
        return "₦" + amount;
    }

    public static String format(BigDecimal amount) {
        return amount == null ? "₦0" : format(amount.longValue());
    }
}
