package com.app.palate.analytics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrdersByStatusDTO {
    private String status;
    private Long count;
}