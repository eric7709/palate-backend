package com.app.palate.notification.services;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.notification.Notification;
import com.app.palate.notification.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarkAllNotificationsAsRead {
    private final NotificationRepository notificationRepository;

    @Transactional
    public void mark(Long accountId) {
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
