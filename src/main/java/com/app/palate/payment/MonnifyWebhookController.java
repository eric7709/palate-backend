package com.app.palate.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.palate.order.Order;
import com.app.palate.order.OrderRepository;
import com.app.palate.order.OrderService;
import com.app.palate.order.OrderStatus;
import com.app.palate.order.UpdateOrderStatusDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class MonnifyWebhookController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/monnify")
    public ResponseEntity<Void> handleWebhook(@RequestBody String rawBody) {
        try {
            log.info("Monnify raw webhook payload: {}", rawBody); // log the full payload

            JsonNode payload = objectMapper.readTree(rawBody);

            // safely check before calling asText()
            if (payload.get("eventType") == null) {
                log.warn("No eventType in payload: {}", rawBody);
                return ResponseEntity.ok().build();
            }

            String eventType = payload.get("eventType").asText();
            log.info("Monnify webhook received: {}", eventType);

            if ("SUCCESSFUL_TRANSACTION".equals(eventType)) {
                JsonNode eventData = payload.get("eventData");
                JsonNode product = eventData.get("product");

                if (product == null || product.get("reference") == null) {
                    log.warn("Missing product or reference in payload");
                    return ResponseEntity.ok().build();
                }
                String invoiceReference = product.get("reference").asText();
                log.info("Monnify invoice reference: {}", invoiceReference);

                Order order = orderRepository.findByMonnifyReference(invoiceReference)
                        .orElse(null);

                if (order != null) {
                    UpdateOrderStatusDTO statusUpdate = new UpdateOrderStatusDTO(OrderStatus.PAID);
                    orderService.updateOrder(order.getId(), statusUpdate);
                    log.info("Order {} automatically marked PAID via Monnify", order.getId());
                } else {
                    log.warn("No order found for Monnify reference: {}", invoiceReference);
                }
            }
        } catch (Exception e) {
            log.error("Error processing Monnify webhook", e);
        }

        return ResponseEntity.ok().build();
    }
}