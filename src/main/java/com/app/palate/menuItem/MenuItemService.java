package com.app.palate.menuItem;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.app.palate.category.Category;
import com.app.palate.category.CategoryRepository;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.utils.EntityResolver;
import com.app.palate.utils.ValidationUtils;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j

@RequiredArgsConstructor
public class MenuItemService {
    private final EntityResolver entityResolver;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemEvents menuItemEvents; // 1. Inject the events component
    private final MenuItemMapper menuItemMapper;
    // CREATE

    public MenuItem createMenuItem(MenuItemRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");

        // Structural Data Validations
        ValidationUtils.requireNonBlank(request.name(), "Menu item name");
        ValidationUtils.requireNonBlank(request.description(), "Description");
        ValidationUtils.requireNonNull(request.categoryId(), "Category ID");
        ValidationUtils.requireGreaterThanZero(request.price(), "Price");

        // Business Rule Validations
        validateUniqueNameOnCreate(request.name());

        Category category = entityResolver.resolveCategory(request.categoryId());

        MenuItemStatus status = request.status() == null ? MenuItemStatus.AVAILABLE : request.status();
        String imageUrl = (request.imageUrl() != null && !request.imageUrl().isBlank())
                ? request.imageUrl().trim()
                : null;

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.name().trim());
        menuItem.setDescription(request.description().trim());
        menuItem.setCategory(category);
        menuItem.setPrice(request.price());
        menuItem.setStatus(status);
        menuItem.setImageUrl(imageUrl);

        MenuItemResponseDTO response = menuItemMapper.toResponse(menuItem);
        menuItemEvents.broadcastCreated(response);
        return menuItemRepository.save(menuItem);
    }

    // BULK CREATE
    @Transactional
    public List<MenuItem> createMenuItemsBulk(List<MenuItemRequestDTO> requests) {
        ValidationUtils.requireNotEmpty(requests, "Bulk request list");

        List<MenuItem> menuItemsToSave = new java.util.ArrayList<>();
        java.util.Set<String> incomingNamesLower = new java.util.HashSet<>();
        java.util.Map<Long, Category> categoryCache = new java.util.HashMap<>();

        // 1. Structural and validation checking
        for (MenuItemRequestDTO req : requests) {
            ValidationUtils.requireNonNull(req, "Bulk item");
            ValidationUtils.requireNonBlank(req.name(), "Menu item name");
            ValidationUtils.requireNonBlank(req.description(), "Description");
            ValidationUtils.requireNonNull(req.categoryId(), "Category ID");
            ValidationUtils.requireGreaterThanZero(req.price(), "Price");

            String normalizedName = req.name().trim().toLowerCase();
            if (!incomingNamesLower.add(normalizedName)) {
                throw new BadRequestException("Duplicate menu item name within request: " + req.name().trim());
            }
        }

        // 2. Map DTOs to entities safely
        for (MenuItemRequestDTO req : requests) {
            validateUniqueNameOnCreate(req.name());

            // Reuse category lookup from cache to stay high-performance
            Category category = categoryCache.computeIfAbsent(req.categoryId(), id -> categoryRepository.findById(id)
                    .orElseThrow(() -> new BadRequestException("Category ID " + id + " not found")));

            MenuItemStatus status = req.status() == null ? MenuItemStatus.AVAILABLE : req.status();
            String imageUrl = (req.imageUrl() != null && !req.imageUrl().isBlank())
                    ? req.imageUrl().trim()
                    : null;

            MenuItem menuItem = new MenuItem();
            menuItem.setName(req.name().trim());
            menuItem.setDescription(req.description().trim());
            menuItem.setCategory(category);
            menuItem.setPrice(req.price());
            menuItem.setStatus(status);
            menuItem.setImageUrl(imageUrl);

            menuItemsToSave.add(menuItem);
        }

        return menuItemRepository.saveAll(menuItemsToSave);
    }

    // UPDATE
    public MenuItem updateMenuItem(Long id, MenuItemRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Menu Item ID");

        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Menu Item not found"));

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new BadRequestException("Category not found"));
            menuItem.setCategory(category);
        }

        if (request.name() != null && !request.name().isBlank()) {
            validateUniqueNameOnUpdate(request.name(), id);
            menuItem.setName(request.name().trim());
        }

        if (request.description() != null && !request.description().isBlank()) {
            menuItem.setDescription(request.description().trim());
        }

        if (request.price() != null) {
            ValidationUtils.requireGreaterThanZero(request.price(), "Price");
            menuItem.setPrice(request.price());
        }

        if (request.imageUrl() != null) {
            menuItem.setImageUrl(request.imageUrl());
        }

        if (request.status() != null) {
            menuItem.setStatus(request.status());
        }
        MenuItemResponseDTO response = menuItemMapper.toResponse(menuItem);
        menuItemEvents.broadcastCreated(response);
        return menuItemRepository.save(menuItem);
    }

    // DELETE
    public void deleteMenuItem(Long id) {
        ValidationUtils.requireNonNull(id, "Menu Item ID");

        if (!menuItemRepository.existsById(id)) {
            throw new BadRequestException("Menu item not found");
        }
        menuItemRepository.deleteById(id);
    }

    public Page<MenuItem> getAllMenuItems(
            String search,
            Long categoryId,
            Boolean isAvailable,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        // 1. Setup sorting parameters
        // 1. Setup sorting with a default fallback
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "createdAt";
        String direction = (sortDirection != null && sortDirection.equalsIgnoreCase("ASC"))
                ? "ASC"
                : "DESC"; // Default to DESC (latest first)

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortByField).descending()
                : Sort.by(sortByField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. Build multi-conditional SQL filter specifications
        Specification<MenuItem> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Text search over item name or description fields
            if (search != null && !search.trim().isEmpty()) {
                String cleanSearch = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), cleanSearch);
                Predicate descMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), cleanSearch);
                predicates.add(criteriaBuilder.or(nameMatch, descMatch));
            }

            // Optional structural filter: by Category ID relationship
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            // Optional structural filter: display active/inactive menu items
            if (isAvailable != null) {
                predicates.add(criteriaBuilder.equal(root.get("isAvailable"), isAvailable));
            }

            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return menuItemRepository.findAll(spec, pageable);
    }

    // GET BY ID
    public MenuItem getMenuItemById(Long id) {
        ValidationUtils.requireNonNull(id, "Menu Item ID");
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Menu item not found"));
    }

    // GET UNAVAILABLE IDS
    public List<Long> getUnavailableMenuItems(List<Long> request) {
        ValidationUtils.requireNotEmpty(request, "Menu item ID list");
        List<Long> unavailableMenuItems = new ArrayList<>();

        for (Long menuItemId : request) {
            ValidationUtils.requireNonNull(menuItemId, "Menu item ID element");
            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new BadRequestException("Menu item not found"));

            if (MenuItemStatus.UNAVAILABLE.equals(menuItem.getStatus())) {
                unavailableMenuItems.add(menuItemId);
            }
        }
        return unavailableMenuItems;
    }

    // GET AVAILABLE IDS
    public List<Long> getAvailableMenuItems(List<Long> request) {
        ValidationUtils.requireNotEmpty(request, "Menu item ID list");
        List<Long> availableMenuItems = new ArrayList<>();
        for (Long menuItemId : request) {
            ValidationUtils.requireNonNull(menuItemId, "Menu item ID element");
            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new BadRequestException("Menu item not found"));
            if (MenuItemStatus.AVAILABLE.equals(menuItem.getStatus())) {
                availableMenuItems.add(menuItemId);
            }
        }
        return availableMenuItems;
    }

    // --- Private Business Custom Assertions ---

    private void validateUniqueNameOnCreate(String name) {
        if (menuItemRepository.existsByNameIgnoreCase(name.trim())) {
            throw new BadRequestException("Menu item with this name already exists");
        }
    }

    private void validateUniqueNameOnUpdate(String name, Long id) {
        if (menuItemRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), id)) {
            throw new BadRequestException("Another menu item with this name already exists");
        }
    }

}