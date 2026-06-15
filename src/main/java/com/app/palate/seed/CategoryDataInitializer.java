package com.app.palate.seed;

import com.app.palate.category.Category;
import com.app.palate.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class CategoryDataInitializer {

    private final CategoryRepository categoryRepository;

    private static final List<CategorySeed> CATEGORY_DUMMY_DATA = List.of(
            new CategorySeed("Breakfast", "Nigerian, English, and Safron Signature breakfasts with sides and extras, served 6:30am-12noon."),
            new CategorySeed("Pastries & Snacks", "Meat pie, chicken pie, doughnuts, and sausages."),
            new CategorySeed("Appetizers", "Chili gizzard, chicken wings, peppered snails, prawns tempura, hummus, and spring rolls."),
            new CategorySeed("The Safron Platters", "Signature, seafood, and mixed sharing platters."),
            new CategorySeed("Nigerian Classics", "Soups, stews, and proteins served with a choice of starch or side."),
            new CategorySeed("Sides (Extra)", "Extra proteins, rice, fries, and vegetable sides."),
            new CategorySeed("Soups", "Cream of chicken, pepper soups, and assorted meat soups."),
            new CategorySeed("Sandwiches, Burgers & Wraps", "Club sandwiches, shawarma, burgers, and grilled cheese steak sandwiches."),
            new CategorySeed("Salads", "Caesar, Greek, garden, seafood, and avocado salads."),
            new CategorySeed("Signature Main Course Dishes", "Cordon blue, grilled croaker, salmon, short ribs, prawn thermidor, and roast chicken."),
            new CategorySeed("From The Grill", "T-bone steak, ribeye, and lamb chops served with choice of sauce and sides."),
            new CategorySeed("Pasta", "Spaghetti bolognese, alfredo, arabiatta, seafood pasta, and fusilli al pesto."),
            new CategorySeed("Pizza", "Chicken, vegetable, suya, and pepperoni pizzas."),
            new CategorySeed("Dessert", "Cheesecake, ice cream, mixed fruit, brownies, and event cakes.")
    );

    @Bean
    @Order(3)
    CommandLineRunner seedCategories() {
        return args -> {
            Set<String> existingNames = new HashSet<>(categoryRepository.findExistingNamesIgnoreCase(
                CATEGORY_DUMMY_DATA.stream().map(seed -> seed.name().toLowerCase()).toList()
            ));

            List<Category> categoriesToSave = CATEGORY_DUMMY_DATA.stream()
                .filter(seed -> !existingNames.contains(seed.name().toLowerCase()))
                .map(seed -> {
                    Category c = new Category();
                    c.setName(seed.name());
                    c.setDescription(seed.description());
                    c.setStatus("ACTIVE");

                    Instant now = Instant.now();
                    c.setCreatedAt(now);
                    c.setUpdatedAt(now);

                    return c;
                })
                .toList();

            if (!categoriesToSave.isEmpty()) {
                categoryRepository.saveAll(categoriesToSave);
                System.out.println("✅ Seeded " + categoriesToSave.size() + " categories");
            } else {
                System.out.println("ℹ️ Categories already seeded");
            }
        };
    }

    private record CategorySeed(String name, String description) {}
}