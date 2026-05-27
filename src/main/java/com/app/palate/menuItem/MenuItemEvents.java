package com.app.palate.menuItem;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuItemEvents {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCreated(MenuItemResponseDTO menuItem) {
        if (menuItem == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/menuItems/created", menuItem);
    }

    public void broadcastUpdated(MenuItemResponseDTO menuItem) {
        if (menuItem == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/menuItems/updated", menuItem);
    }

    public void broadcastDeleted(Long id) {
        if (id == null) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/menuItems/deleted", id);
    }
}
