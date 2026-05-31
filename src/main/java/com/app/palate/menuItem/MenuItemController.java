package com.app.palate.menuItem;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/palate/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;
    private final MenuItemEvents menuItemEvents;
    // 1. Inject the new non-static MapStruct Mapper here
    private final MenuItemMapper menuItemMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponseDTO createMenuItem(@RequestBody MenuItemRequestDTO request) {
        MenuItem menuItem = menuItemService.createMenuItem(request);
        // 2. Use the injected bean instance instead of static calls
        MenuItemResponseDTO response = menuItemMapper.toResponse(menuItem);

        menuItemEvents.broadcastCreated(response);
        return response;
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<MenuItem> createMenuItemsBulk(@RequestBody List<MenuItemRequestDTO> requests) {
        return menuItemService.createMenuItemsBulk(requests);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<MenuItemResponseDTO> getAllMenuItems(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Page<MenuItem> menuItemsPage = menuItemService.getAllMenuItems(
                search, categoryId, isAvailable, page, size, sortBy, sortDirection);
        return menuItemsPage.map(menuItemMapper::toResponse);
    }

    @PostMapping("/unavailable")
    @ResponseStatus(HttpStatus.OK)
    public List<Long> getUnavailbleMenuItems(@RequestBody List<Long> request) {
        return menuItemService.getUnavailableMenuItems(request);
    }

    @GetMapping("/available")
    @ResponseStatus(HttpStatus.OK)
    public List<Long> getAvailbleMenuItems(@RequestBody List<Long> request) {
        return menuItemService.getAvailableMenuItems(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MenuItemResponseDTO updateMenuItem(@PathVariable Long id, @RequestBody MenuItemRequestDTO request) {
        MenuItem menuItem = menuItemService.updateMenuItem(id, request);
        // 4. Update to instance call
        MenuItemResponseDTO response = menuItemMapper.toResponse(menuItem);
        menuItemEvents.broadcastUpdated(response);
        return response;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MenuItemResponseDTO getMenuItemById(@PathVariable Long id) {
        MenuItem menuItem = menuItemService.getMenuItemById(id);
        // 5. Update to instance call
        return menuItemMapper.toResponse(menuItem);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        menuItemEvents.broadcastDeleted(id);
    }
}