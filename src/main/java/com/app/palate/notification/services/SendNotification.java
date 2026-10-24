package com.app.palate.notification.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.app.palate.auth.Account;
import com.app.palate.notification.Notification;
import com.app.palate.notification.NotificationRepository;

import lombok.RequiredArgsConstructor;

/**
 * Saves a notification for an account and pushes it to them live over the websocket.
 *
 * Deliberately not @Transactional: the notification must be committed before it is pushed,
 * otherwise a client could react to it before it exists in the database.
 */
@Service
@RequiredArgsConstructor
public class SendNotification {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void execute(Account account, String title, String type, String message) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setType(type);
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setIsRead(false);

        notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(
                String.valueOf(account.getId()),
                "/queue/notifications",
                notification
        );
    }
}
