package com.app.palate.notification.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.notification.Notification;
import com.app.palate.notification.NotificationRepository;

import lombok.RequiredArgsConstructor;

/** An account's notifications, newest first. */
@Service
@RequiredArgsConstructor
public class GetNotificationsForAccount {
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<Notification> get(Long accountId) {
        return notificationRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }
}
