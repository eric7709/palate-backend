package com.app.palate.notification.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.notification.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetUnreadNotificationCount {
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public long get(Long accountId) {
        return notificationRepository.countByAccountIdAndIsReadFalse(accountId);
    }
}
