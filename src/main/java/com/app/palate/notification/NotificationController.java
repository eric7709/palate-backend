package com.app.palate.notification;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.app.palate.auth.Account;
import com.app.palate.notification.services.GetNotificationsForAccount;
import com.app.palate.notification.services.GetUnreadNotificationCount;
import com.app.palate.notification.services.MarkAllNotificationsAsRead;
import com.app.palate.notification.services.MarkNotificationAsRead;
import com.app.palate.notification.utils.CurrentAccountResolver;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/palate/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final GetNotificationsForAccount getNotificationsForAccount;
    private final GetUnreadNotificationCount getUnreadNotificationCount;
    private final MarkNotificationAsRead markNotificationAsRead;
    private final MarkAllNotificationsAsRead markAllNotificationsAsRead;
    private final CurrentAccountResolver currentAccountResolver;

    // GET /api/palate/notifications
    @GetMapping
    public ResponseEntity<List<Notification>> getMine(
            @RequestHeader("Authorization") String authHeader) {
        Account account = currentAccountResolver.resolve(authHeader);
        return ResponseEntity.ok(getNotificationsForAccount.get(account.getId()));
    }

    // GET /api/palate/notifications/unread-count
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("Authorization") String authHeader) {
        Account account = currentAccountResolver.resolve(authHeader);
        return ResponseEntity.ok(Map.of("count", getUnreadNotificationCount.get(account.getId())));
    }

    // PATCH /api/palate/notifications/{id}/read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(markNotificationAsRead.mark(id));
    }

    // PATCH /api/palate/notifications/read-all
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("Authorization") String authHeader) {
        Account account = currentAccountResolver.resolve(authHeader);
        markAllNotificationsAsRead.mark(account.getId());
        return ResponseEntity.noContent().build();
    }
}
