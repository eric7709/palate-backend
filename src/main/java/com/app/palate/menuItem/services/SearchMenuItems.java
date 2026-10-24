package com.app.palate.menuItem.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.menuItem.MenuItem;
import com.app.palate.menuItem.MenuItemRepository;
import com.app.palate.menuItem.dto.MenuItemSearchCriteria;
import com.app.palate.menuItem.enums.MenuItemStatus;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

/**
 * Lists menu items one page at a time. The search text is matched against the item and its
 * category; category, status, availability and price range narrow the results further.
 * Sorted newest first unless the caller asks for ascending.
 */
@Service
@RequiredArgsConstructor
public class SearchMenuItems {
    private final MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public Page<MenuItem> search(MenuItemSearchCriteria criteria) {
        String sortByField = (criteria.getSortBy() != null && !criteria.getSortBy().isEmpty())
                ? criteria.getSortBy() : "createdAt";
        String direction = (criteria.getSortDirection() != null
                && criteria.getSortDirection().equalsIgnoreCase("ASC"))
                ? "ASC"
                : "DESC";

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortByField).descending()
                : Sort.by(sortByField).ascending();

        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        return menuItemRepository.findAll(matching(criteria), pageable);
    }

    private Specification<MenuItem> matching(MenuItemSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String cleanSearch = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), cleanSearch);
                Predicate descMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), cleanSearch);
                Predicate imageMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("imageUrl")), cleanSearch);
                Predicate statusMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("status").as(String.class)), cleanSearch);
                Predicate priceMatch = criteriaBuilder.like(
                        root.get("price").as(String.class), cleanSearch);
                Predicate idMatch = criteriaBuilder.like(root.get("id").as(String.class), cleanSearch);
                Predicate createdAtMatch = criteriaBuilder.like(
                        root.get("createdAt").as(String.class), cleanSearch);
                Predicate updatedAtMatch = criteriaBuilder.like(
                        root.get("updatedAt").as(String.class), cleanSearch);
                var categoryJoin = root.join("category", JoinType.LEFT);
                Predicate categoryNameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(categoryJoin.get("name")), cleanSearch);
                Predicate categoryDescriptionMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(categoryJoin.get("description")), cleanSearch);
                predicates.add(criteriaBuilder.or(nameMatch, descMatch, imageMatch, statusMatch,
                        priceMatch, idMatch, createdAtMatch, updatedAtMatch,
                        categoryNameMatch, categoryDescriptionMatch));
                query.distinct(true);
            }

            if (criteria.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), criteria.getCategoryId()));
            }

            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getIsAvailable() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"),
                        criteria.getIsAvailable() ? MenuItemStatus.AVAILABLE : MenuItemStatus.UNAVAILABLE));
            }
            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"), criteria.getMaxPrice()));
            }

            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
