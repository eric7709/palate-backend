package com.app.palate.category;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.app.palate.category.dto.CategoryRequestDTO;
import com.app.palate.category.dto.CategoryResponseDTO;
import com.app.palate.category.dto.CategorySearchCriteria;
import com.app.palate.category.services.CreateCategoriesBulk;
import com.app.palate.category.services.CreateCategory;
import com.app.palate.category.services.DeleteCategory;
import com.app.palate.category.services.GetCategoryById;
import com.app.palate.category.services.SearchCategories;
import com.app.palate.category.services.UpdateCategory;
import com.app.palate.category.utils.CategoryMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/palate/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CreateCategory createCategory;
    private final CreateCategoriesBulk createCategoriesBulk;
    private final UpdateCategory updateCategory;
    private final SearchCategories searchCategories;
    private final GetCategoryById getCategoryById;
    private final DeleteCategory deleteCategory;
    private final CategoryMapper categoryMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDTO create(@RequestBody CategoryRequestDTO request) {
        return categoryMapper.mapToDto(createCategory.create(request));
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<CategoryResponseDTO> createBulk(@RequestBody List<CategoryRequestDTO> requests) {
        return createCategoriesBulk.create(requests).stream()
                .map(categoryMapper::mapToDto)
                .toList();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryResponseDTO update(@PathVariable Long id, @RequestBody CategoryRequestDTO request) {
        return categoryMapper.mapToDto(updateCategory.update(id, request));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<CategoryResponseDTO> search(CategorySearchCriteria criteria) {
        return searchCategories.search(criteria).map(categoryMapper::mapToDto);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryResponseDTO get(@PathVariable Long id) {
        return categoryMapper.mapToDto(getCategoryById.get(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteCategory.delete(id);
    }
}
