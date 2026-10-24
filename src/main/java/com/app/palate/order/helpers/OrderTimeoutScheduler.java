package com.app.palate.order.helpers;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.auth.enums.Role;
import com.app.palate.notification.services.SendNotification;
import com.app.palate.order.Order;
import com.app.palate.order.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderRepository orderRepository;
    private final SendNotification sendNotification;
    private final AccountRepository accountRepository;

    @Scheduled(fixedRate = 60000)
    public void checkStaleOrders() {
        Instant cutoff = Instant.now().minus(10, ChronoUnit.MINUTES);

        List<Order> staleOrders = orderRepository.findStaleOrders(cutoff);

        if (staleOrders.isEmpty()) return;

        log.warn("⚠️ {} stale order(s) found exceeding 10 minute response threshold", staleOrders.size());

        List<Account> admins = accountRepository.findByRole(Role.ROLE_ADMIN);

        for (Order order : staleOrders) {
            String title   = "Order Not Responded To";
            String type    = "STALE_ORDER";
            String message = "Order " + order.getInvoiceNumber() + " has been pending for over 10 minutes.";

            for (Account admin : admins) {
                sendNotification.execute(admin, title, type, message);
            }

            log.warn("  -> Notified {} admin(s) for order #{} | Invoice: {}",
                    admins.size(), order.getId(), order.getInvoiceNumber());
        }
    }
}