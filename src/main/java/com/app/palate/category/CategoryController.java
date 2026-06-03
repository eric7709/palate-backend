package com.app.palate.category;

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
@RequestMapping("/api/palate/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDTO createCategory(@RequestBody CategoryRequestDTO request) {
        Category category = categoryService.createCategory(request);
        return categoryMapper.mapToDto(category); // 2. Updated to use instance variable
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryResponseDTO updateCategory(@PathVariable Long id, @RequestBody CategoryRequestDTO request) {
        Category category = categoryService.updateCategory(id, request);
        return categoryMapper.mapToDto(category); // 3. Updated to use instance variable
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<CategoryResponseDTO> getAllCategories(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Page<Category> categoryPage = categoryService.getAllCategories(
                search, page, size, sortBy, sortDirection);
        return categoryPage.map(categoryMapper::mapToDto);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryResponseDTO getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return categoryMapper.mapToDto(category); // 4. Updated to use instance variable
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<CategoryResponseDTO> createCategoriesBulk(@RequestBody List<CategoryRequestDTO> categories) {
        List<Category> savedCategories = categoryService.createCategoriesBulk(categories);
        return savedCategories.stream()
                .map(categoryMapper::mapToDto) // 5. Updated method reference to use instance variable
                .toList();
    }
}