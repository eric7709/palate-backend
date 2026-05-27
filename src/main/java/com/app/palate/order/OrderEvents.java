package com.app.palate.order;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEvents {
    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(OrderResponseDTO order) {
        if (order == null) {
            log.warn("broadcastCreated called with null order. Skipping.");
            return;
        }
        Long customerId = order.getCustomer().getId();
        // 🔊 Global (admins / kitchen)
        messagingTemplate.convertAndSend("/topic/orders/created", order);
        // 🎯 Customer-specific
        if (customerId != null) {
            messagingTemplate.convertAndSend("/topic/orders/customer/" + customerId, order);
        }
    }

    public void broadcastUpdated(OrderResponseDTO order) {
        if (order == null) {
            log.warn("broadcastUpdated called with null order. Skipping.");
            return;
        }
        Long customerId = order.getCustomer().getId();
        // 🔊 Global (admins / kitchen)
        messagingTemplate.convertAndSend("/topic/orders/updated", order);

        // 🎯 Customer-specific
        if (customerId != null) {
            messagingTemplate.convertAndSend(
                    "/topic/orders/customer/" + customerId,
                    order);
        }
    }
}
