package com.app.palate.category.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.category.Category;
import com.app.palate.category.CategoryRepository;
import com.app.palate.category.dto.CategorySearchCriteria;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

/**
 * Lists categories one page at a time. The search text is matched against the category's own
 * fields and the names of its menu items; the status filter is an exact match.
 */
@Service
@RequiredArgsConstructor
public class SearchCategories {
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<Category> search(CategorySearchCriteria criteria) {
        Sort sort = criteria.getSortDirection().equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(criteria.getSortBy()).descending()
                : Sort.by(criteria.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        return categoryRepository.findAll(matching(criteria), pageable);
    }

    private Specification<Category> matching(CategorySearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String cleanSearch = "%" + criteria.getSearch().trim().toLowerCase() + "%";

                Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), cleanSearch);
                Predicate descriptionMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), cleanSearch);
                Predicate statusMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("status")), cleanSearch);
                Predicate idMatch = criteriaBuilder.like(root.get("id").as(String.class), cleanSearch);
                Predicate createdAtMatch = criteriaBuilder.like(
                        root.get("createdAt").as(String.class), cleanSearch);
                Predicate updatedAtMatch = criteriaBuilder.like(
                        root.get("updatedAt").as(String.class), cleanSearch);
                var menuItemJoin = root.join("menuItems", JoinType.LEFT);
                Predicate menuItemNameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(menuItemJoin.get("name")), cleanSearch);
                predicates.add(criteriaBuilder.or(nameMatch, descriptionMatch, statusMatch,
                        idMatch, createdAtMatch, updatedAtMatch, menuItemNameMatch));
                query.distinct(true);
            }

            if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus().trim()));
            }

            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
