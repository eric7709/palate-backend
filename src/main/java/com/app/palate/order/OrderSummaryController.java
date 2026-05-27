package com.app.palate.order;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/palate/orders/summary")
@RequiredArgsConstructor
public class OrderSummaryController {

    private final OrderSummaryService orderSummaryService;

    @GetMapping
    public ResponseEntity<OrderSummaryResponse> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long waiterId,
            @RequestParam(required = false) Long cashierId
        ) {
        LocalDate start = startDate != null ? startDate : LocalDate.now();
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return ResponseEntity.ok(orderSummaryService.getSummaryByDate(start, end, waiterId, cashierId));
    }
}