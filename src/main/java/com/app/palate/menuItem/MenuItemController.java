package com.app.palate.menuItem;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.app.palate.menuItem.dto.MenuItemRequestDTO;
import com.app.palate.menuItem.dto.MenuItemResponseDTO;
import com.app.palate.menuItem.dto.MenuItemSearchCriteria;
import com.app.palate.menuItem.services.CreateMenuItem;
import com.app.palate.menuItem.services.CreateMenuItemsBulk;
import com.app.palate.menuItem.services.DeleteMenuItem;
import com.app.palate.menuItem.services.GetAvailableMenuItems;
import com.app.palate.menuItem.services.GetMenuItemById;
import com.app.palate.menuItem.services.GetUnavailableMenuItems;
import com.app.palate.menuItem.services.SearchMenuItems;
import com.app.palate.menuItem.services.UpdateMenuItem;
import com.app.palate.menuItem.utils.MenuItemMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/palate/menu-items")
public class MenuItemController {
    private final CreateMenuItem createMenuItem;
    private final CreateMenuItemsBulk createMenuItemsBulk;
    private final UpdateMenuItem updateMenuItem;
    private final SearchMenuItems searchMenuItems;
    private final GetMenuItemById getMenuItemById;
    private final GetUnavailableMenuItems getUnavailableMenuItems;
    private final GetAvailableMenuItems getAvailableMenuItems;
    private final DeleteMenuItem deleteMenuItem;
    private final MenuItemEvents menuItemEvents;
    private final MenuItemMapper menuItemMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponseDTO create(@RequestBody MenuItemRequestDTO request) {
        MenuItemResponseDTO response = menuItemMapper.toResponse(createMenuItem.create(request));

        menuItemEvents.broadcastCreated(response);
        return response;
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<MenuItem> createBulk(@RequestBody List<MenuItemRequestDTO> requests) {
        return createMenuItemsBulk.create(requests);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<MenuItemResponseDTO> search(MenuItemSearchCriteria criteria) {
        return searchMenuItems.search(criteria).map(menuItemMapper::toResponse);
    }

    @PostMapping("/unavailable")
    @ResponseStatus(HttpStatus.OK)
    public List<Long> getUnavailable(@RequestBody List<Long> menuItemIds) {
        return getUnavailableMenuItems.get(menuItemIds);
    }

    @GetMapping("/available")
    @ResponseStatus(HttpStatus.OK)
    public List<Long> getAvailable(@RequestBody List<Long> menuItemIds) {
        return getAvailableMenuItems.get(menuItemIds);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MenuItemResponseDTO update(@PathVariable Long id, @RequestBody MenuItemRequestDTO request) {
        MenuItemResponseDTO response = menuItemMapper.toResponse(updateMenuItem.update(id, request));

        menuItemEvents.broadcastUpdated(response);
        return response;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MenuItemResponseDTO get(@PathVariable Long id) {
        return menuItemMapper.toResponse(getMenuItemById.get(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteMenuItem.delete(id);
        menuItemEvents.broadcastDeleted(id);
    }
}
