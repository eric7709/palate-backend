package com.app.palate.payment.services;

import org.springframework.stereotype.Service;

import com.app.palate.order.Order;
import com.app.palate.order.OrderRepository;
import com.app.palate.order.services.UpdateOrder;
import com.app.palate.order.dto.UpdateOrderStatusDTO;
import com.app.palate.order.enums.OrderStatus;
import com.app.palate.payment.dto.MonnifyInvoiceResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Applies a payment notification from Monnify to its order. Anything wrong with the notification
 * is logged and dropped rather than thrown, so Monnify always gets a success reply and does not
 * keep re-sending it.
 *
 * Deliberately not @Transactional: errors are swallowed here, and a swallowed error inside a
 * transaction would make Spring roll back the whole thing anyway.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HandleMonnifyWebhook {
    private final UpdateOrder updateOrder;
    private final OrderRepository orderRepository;
    private final CreateMonnifyInvoice createMonnifyInvoice;
    private final ObjectMapper objectMapper;

    public void handle(String rawBody) {
        try {
            log.info("Monnify raw webhook payload: {}", rawBody);

            JsonNode payload = objectMapper.readTree(rawBody);

            if (payload.get("eventType") == null) {
                log.warn("No eventType in payload: {}", rawBody);
                return;
            }

            String eventType = payload.get("eventType").asText();
            log.info("Monnify webhook received: {}", eventType);

            if (!"SUCCESSFUL_TRANSACTION".equals(eventType)) {
                return;
            }

            JsonNode eventData = payload.get("eventData");
            JsonNode product = eventData != null ? eventData.get("product") : null;

            if (product == null || product.get("reference") == null) {
                log.warn("Missing product or reference in payload");
                return;
            }

            String invoiceReference = product.get("reference").asText();
            log.info("Monnify invoice reference: {}", invoiceReference);

            Order order = orderRepository.findByMonnifyReference(invoiceReference).orElse(null);

            if (order == null) {
                log.warn("No order found for Monnify reference: {}", invoiceReference);
                return;
            }

            JsonNode amountPaidNode = eventData.get("amountPaid");
            if (amountPaidNode == null) {
                log.warn("No amountPaid in payload for order {}", order.getId());
                return;
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
    }

    /** Covers both a single full payment and the final installment of a previously underpaid order. */
    private void handleExactPayment(Order order) {
        order.setIsUnderpaid(false);
        order.setRemainingBalance(null);
        orderRepository.save(order);

        UpdateOrderStatusDTO statusUpdate = new UpdateOrderStatusDTO(OrderStatus.PAID);
        updateOrder.execute(order.getId(), statusUpdate);

        log.info("Order {} marked PAID via Monnify (total paid: {})", order.getId(), order.getPaidAmount());
    }

    /** Records what is still owed and issues a new virtual account for exactly that amount. */
    private void handleUnderpayment(Order order, double amountPaid, double expectedAmount) {
        double newRemainingBalance = expectedAmount - amountPaid;

        log.warn("Underpayment for order {}: this payment={}, expected={}, new remaining balance={}",
                order.getId(), amountPaid, expectedAmount, newRemainingBalance);

        order.setIsUnderpaid(true);
        order.setRemainingBalance(newRemainingBalance);

        regenerateVirtualAccount(order, newRemainingBalance, "underpayment");
    }

    /** Marks the order paid, but flags that the difference may need to be refunded. */
    private void handleOverpayment(Order order, double amountPaid, double expectedAmount) {
        double overpaidBy = amountPaid - expectedAmount;

        log.warn("Overpayment for order {}: this payment={}, expected={} (overpaid by {})",
                order.getId(), amountPaid, expectedAmount, overpaidBy);

        order.setIsUnderpaid(false);
        order.setRemainingBalance(null);
        orderRepository.save(order);

        UpdateOrderStatusDTO statusUpdate = new UpdateOrderStatusDTO(OrderStatus.PAID);
        updateOrder.execute(order.getId(), statusUpdate);

        // TODO: trigger a refund workflow for `overpaidBy`, or flag for manual review
        log.info("Order {} marked PAID despite overpayment. Refund of {} may be required.",
                order.getId(), overpaidBy);
    }

    /** Monnify virtual accounts are single-use, so each new amount due needs a new one. */
    private void regenerateVirtualAccount(Order order, double amountDue, String reason) {
        try {
            String customerName = order.getCustomer() != null
                    ? order.getCustomer().getTitle() + " " + order.getCustomer().getName()
                    : "Customer";

            MonnifyInvoiceResponse newInvoice = createMonnifyInvoice.execute(
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
