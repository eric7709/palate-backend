package com.app.palate.category;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.utils.ValidationUtils;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // CREATE
    public Category createCategory(CategoryRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getName(), "Category name");

        validateUniqueNameOnCreate(request.getName());

        Category category = new Category();
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");

        return categoryRepository.save(category);
    }

    // UPDATE
    public Category updateCategory(Long id, CategoryRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Category ID");
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getName(), "Category name");

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Category not found"));

        validateUniqueNameOnUpdate(request.getName(), id);

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");

        return categoryRepository.save(category);
    }

    // GET ALL
    public Page<Category> getAllCategories(
            String search,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        // 1. Setup sorting options safely
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. Build multi-conditional structural text search
        Specification<Category> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String cleanSearch = "%" + search.trim().toLowerCase() + "%";

                // Assuming your Category has a 'name' field (and optional 'description')
                Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), cleanSearch);
                predicates.add(nameMatch);
            }

            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return categoryRepository.findAll(spec, pageable);
    }

    // GET BY ID
    public Category getCategoryById(Long id) {
        ValidationUtils.requireNonNull(id, "Category ID");

        return categoryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Category not found"));
    }

    // DELETE
    public void deleteCategory(Long id) {
        ValidationUtils.requireNonNull(id, "Category ID");

        if (!categoryRepository.existsById(id)) {
            throw new BadRequestException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    // BULK CREATE
    @Transactional // Added to ensure bulk saves roll back completely if a validation fails mid-way
    public List<Category> createCategoriesBulk(List<CategoryRequestDTO> requests) {
        ValidationUtils.requireNotEmpty(requests, "Request list");

        Set<String> requestNamesLower = new HashSet<>();
        for (CategoryRequestDTO req : requests) {
            ValidationUtils.requireNonNull(req, "Category request item");
            ValidationUtils.requireNonBlank(req.getName(), "Category name");

            String normalized = req.getName().trim().toLowerCase();
            if (!requestNamesLower.add(normalized)) {
                throw new BadRequestException("Duplicate category name in request: " + req.getName().trim());
            }
        }

        List<String> existingNames = categoryRepository.findExistingNamesIgnoreCase(new ArrayList<>(requestNamesLower));
        if (!existingNames.isEmpty()) {
            throw new BadRequestException("Categories already exist: " + String.join(", ", existingNames));
        }

        List<Category> categories = requests.stream()
                .map(req -> {
                    Category cat = new Category();
                    cat.setName(req.getName().trim());
                    cat.setDescription(req.getDescription() != null ? req.getDescription().trim() : "");
                    return cat;
                })
                .toList();

        return categoryRepository.saveAll(categories);
    }

    // --- Private Business Custom Assertions ---

    private void validateUniqueNameOnCreate(String name) {
        if (categoryRepository.existsByNameIgnoreCase(name.trim())) {
            throw new BadRequestException("Category with this name already exists");
        }
    }

    private void validateUniqueNameOnUpdate(String name, Long id) {
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), id)) {
            throw new BadRequestException("Another category with this name already exists");
        }
    }
}