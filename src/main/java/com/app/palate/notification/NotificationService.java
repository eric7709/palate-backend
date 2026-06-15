package com.app.palate.notification;

import com.app.palate.auth.Account;
import com.app.palate.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

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

        messagingTemplate.convertAndSendToUser(
                String.valueOf(account.getId()),
                "/queue/notifications",
                notification
        );
    }

    public List<Notification> getNotificationsForAccount(Long accountId) {
        return notificationRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    public long getUnreadCount(Long accountId) {
        return notificationRepository.countByAccountIdAndIsReadFalse(accountId);
    }

    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setIsRead(true);
        notification.setReadAt(Instant.now());

        return notificationRepository.save(notification);
    }

    public void markAllAsRead(Long accountId) {
        List<Notification> unread = notificationRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .filter(n -> !n.getIsRead())
                .toList();

        unread.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(Instant.now());
        });

        notificationRepository.saveAll(unread);
    }
}