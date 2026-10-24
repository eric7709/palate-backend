package com.app.palate.notification.services;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.exceptions.ResourceNotFoundException;
import com.app.palate.notification.Notification;
import com.app.palate.notification.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarkNotificationAsRead {
    private final NotificationRepository notificationRepository;

    @Transactional
    @SuppressWarnings("null")
    public Notification mark(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setIsRead(true);
        notification.setReadAt(Instant.now());

        return notificationRepository.save(notification);
    }
}
