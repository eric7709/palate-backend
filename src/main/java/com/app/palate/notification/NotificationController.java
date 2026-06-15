package com.app.palate.notification;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/palate/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;

    private Account resolveAccount(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Missing or invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String email = jwtUtil.getEmail(token);
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Account not found"));
    }

    // GET /api/palate/notifications
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(
            @RequestHeader("Authorization") String authHeader) {
        Account account = resolveAccount(authHeader);
        return ResponseEntity.ok(notificationService.getNotificationsForAccount(account.getId()));
    }

    // GET /api/palate/notifications/unread-count
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("Authorization") String authHeader) {
        Account account = resolveAccount(authHeader);
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(account.getId())));
    }

    // PATCH /api/palate/notifications/{id}/read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    // PATCH /api/palate/notifications/read-all
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("Authorization") String authHeader) {
        Account account = resolveAccount(authHeader);
        notificationService.markAllAsRead(account.getId());
        return ResponseEntity.noContent().build();
    }
}