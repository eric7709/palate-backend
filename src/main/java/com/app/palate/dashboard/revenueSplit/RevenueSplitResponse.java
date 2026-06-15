package com.app.palate.dashboard.revenueSplit;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RevenueSplitResponse {

    private final List<RevenueSplitItem> data;
    private final int restaurantShare;

    @Getter
    @AllArgsConstructor
    public static class RevenueSplitItem {
        private final String label;
        private final String value;
        private final int percent;
    }
}