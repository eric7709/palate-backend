package com.app.palate.order;

import org.springframework.data.domain.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderPageResponse {
    private Page<OrderResponseDTO> orders;
    private OrderStatusCounts statusCounts;
}