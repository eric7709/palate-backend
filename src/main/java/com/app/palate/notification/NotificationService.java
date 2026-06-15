package com.app.palate.notification;

import com.app.palate.auth.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendToAccount(Account account, String title, String type, String message) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setType(type);
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setIsRead(false);

        notificationRepository.save(notification);

        // Push to specific account via WebSocket
        messagingTemplate.convertAndSendToUser(
                String.valueOf(account.getId()),
                "/queue/notifications",
                notification
        );
    }
}