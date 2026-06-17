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
    private final MonnifyService monnifyService;
    private final ObjectMapper objectMapper;

    @PostMapping("/monnify")
    public ResponseEntity<Void> handleWebhook(@RequestBody String rawBody) {
        try {
            log.info("Monnify raw webhook payload: {}", rawBody);

            JsonNode payload = objectMapper.readTree(rawBody);

            if (payload.get("eventType") == null) {
                log.warn("No eventType in payload: {}", rawBody);
                return ResponseEntity.ok().build();
            }

            String eventType = payload.get("eventType").asText();
            log.info("Monnify webhook received: {}", eventType);

            if (!"SUCCESSFUL_TRANSACTION".equals(eventType)) {
                return ResponseEntity.ok().build();
            }

            JsonNode eventData = payload.get("eventData");
            JsonNode product = eventData != null ? eventData.get("product") : null;

            if (product == null || product.get("reference") == null) {
                log.warn("Missing product or reference in payload");
                return ResponseEntity.ok().build();
            }

            String invoiceReference = product.get("reference").asText();
            log.info("Monnify invoice reference: {}", invoiceReference);

            Order order = orderRepository.findByMonnifyReference(invoiceReference).orElse(null);

            if (order == null) {
                log.warn("No order found for Monnify reference: {}", invoiceReference);
                return ResponseEntity.ok().build();
            }

            JsonNode amountPaidNode = eventData.get("amountPaid");
            if (amountPaidNode == null) {
                log.warn("No amountPaid in payload for order {}", order.getId());
                return ResponseEntity.ok().build();
            }

            double amountPaid = amountPaidNode.asDouble();

            // If this order already has an outstanding remaining balance from a prior
            // underpayment, compare against THAT, not the original order total.
            boolean hasPriorUnderpayment = Boolean.TRUE.equals(order.getIsUnderpaid())
                    && order.getRemainingBalance() != null;

            double expectedAmount = hasPriorUnderpayment
                    ? order.getRemainingBalance()
                    : order.getTotal();

            // Accumulate paidAmount across multiple webhook calls instead of overwriting it.
            double previouslyPaid = order.getPaidAmount() != null ? order.getPaidAmount() : 0;
            double totalPaidSoFar = previouslyPaid + amountPaid;
            order.setPaidAmount(totalPaidSoFar);

            log.info("Order {}: this payment={}, expected={}, totalPaidSoFar={}",
                    order.getId(), amountPaid, expectedAmount, totalPaidSoFar);

            if (amountPaid == expectedAmount) {
                handleExactPayment(order);
            } else if (amountPaid < expectedAmount) {
                handleUnderpayment(order, amountPaid, expectedAmount);
            } else {
                handleOverpayment(order, amountPaid, expectedAmount);
            }

        } catch (Exception e) {
            log.error("Error processing Monnify webhook", e);
        }

        return ResponseEntity.ok().build();
    }

    // =======================
    // EXACT PAYMENT — covers both a single full payment AND the final
    // installment that completes a previously underpaid order.
    // =======================
    private void handleExactPayment(Order order) {
        order.setIsUnderpaid(false);
        order.setRemainingBalance(null);
        orderRepository.save(order);

        UpdateOrderStatusDTO statusUpdate = new UpdateOrderStatusDTO(OrderStatus.PAID);
        orderService.updateOrder(order.getId(), statusUpdate);

        log.info("Order {} marked PAID via Monnify (total paid: {})", order.getId(), order.getPaidAmount());
    }

    // =======================
    // UNDERPAYMENT — regenerate virtual account for the NEW remaining balance
    // =======================
    private void handleUnderpayment(Order order, double amountPaid, double expectedAmount) {
        double newRemainingBalance = expectedAmount - amountPaid;

        log.warn("Underpayment for order {}: this payment={}, expected={}, new remaining balance={}",
                order.getId(), amountPaid, expectedAmount, newRemainingBalance);

        order.setIsUnderpaid(true);
        order.setRemainingBalance(newRemainingBalance);

        regenerateVirtualAccount(order, newRemainingBalance, "underpayment");
    }

    // =======================
    // OVERPAYMENT — mark paid, but flag for refund of the difference
    // =======================
    private void handleOverpayment(Order order, double amountPaid, double expectedAmount) {
        double overpaidBy = amountPaid - expectedAmount;

        log.warn("Overpayment for order {}: this payment={}, expected={} (overpaid by {})",
                order.getId(), amountPaid, expectedAmount, overpaidBy);

        order.setIsUnderpaid(false);
        order.setRemainingBalance(null);
        orderRepository.save(order);

        UpdateOrderStatusDTO statusUpdate = new UpdateOrderStatusDTO(OrderStatus.PAID);
        orderService.updateOrder(order.getId(), statusUpdate);

        // TODO: trigger a refund workflow for `overpaidBy`, or flag for manual review
        log.info("Order {} marked PAID despite overpayment. Refund of {} may be required.",
                order.getId(), overpaidBy);
    }

    // =======================
    // REGENERATE VIRTUAL ACCOUNT (since Monnify accounts are single-use)
    // =======================
    private void regenerateVirtualAccount(Order order, double amountDue, String reason) {
        try {
            String customerName = order.getCustomer() != null
                    ? order.getCustomer().getTitle() + " " + order.getCustomer().getName()
                    : "Customer";

            MonnifyInvoiceResponse newInvoice = monnifyService.createOrderInvoice(
                    order.getId(), amountDue, customerName);

            order.setVirtualAccountNumber(newInvoice.accountNumber());
            order.setVirtualBankName(newInvoice.bankName());
            order.setMonnifyReference(newInvoice.invoiceReference());

            orderRepository.save(order);

            log.info("New virtual account generated for order {} due to {}: {} - {} (amount due: {})",
                    order.getId(), reason, newInvoice.bankName(), newInvoice.accountNumber(), amountDue);

        } catch (Exception e) {
            log.error("Failed to regenerate virtual account for order {} after {}", order.getId(), reason, e);
        }
    }
}