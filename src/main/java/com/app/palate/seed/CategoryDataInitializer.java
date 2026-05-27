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
            new CategorySeed("Rice Dishes", "Flavorful rice meals including jollof, fried, coconut, ofada, and native rice."),
            new CategorySeed("Swallows", "Starchy dough meals like pounded yam, eba, fufu, amala, semovita."),
            new CategorySeed("Soups & Stews", "Rich Nigerian soups such as egusi, ogbono, okra, banga, and vegetable stews."),
            new CategorySeed("Grilled & Fried Proteins", "Grilled/fried meats and fish like suya, peppered meat, asun, grilled fish."),
            new CategorySeed("Snacks & Street Food", "Street foods like puff-puff, meat pies, akara, chin-chin, samosas."),
            new CategorySeed("Beans & Legumes", "Meals from beans and legumes: moi moi, akara, ewa agoyin, bean porridge."),
            new CategorySeed("Breakfast Specials", "Morning meals: yam porridge, akara & bread, pap (ogi), fried plantains."),
            new CategorySeed("Drinks & Beverages", "Zobo, kunu, chapman, smoothies, milkshakes, fresh juices."),
            new CategorySeed("Desserts & Pastries", "Chin-chin, puff-puff, coconut candy, doughnut, fruit salad."),
            new CategorySeed("Seafood", "Peppered prawns, catfish pepper soup, grilled tilapia, crab, periwinkle."),
            new CategorySeed("Pepper Soups", "Goat meat, catfish, chicken pepper soups."),
            new CategorySeed("Pasta & Noodles", "Spaghetti jollof, macaroni bake, indomie stir fry."),
            new CategorySeed("Porridges", "Yam, plantain, beans, and corn porridge (ogi)."),
            new CategorySeed("Side Dishes", "Fried plantains, coleslaw, steamed vegetables, garden salad."),
            new CategorySeed("Pizza", "Margherita, pepperoni, BBQ chicken, vegetarian, Hawaiian pizzas."),
            new CategorySeed("Burgers & Sandwiches", "Beef, chicken, fish burgers; club sandwiches, wraps."),
            new CategorySeed("Fast Food", "Fried chicken, nuggets, french fries, hot dogs."),
            new CategorySeed("Continental Dishes", "Steaks, grilled chicken, fish & chips, lasagna, pasta."),
            new CategorySeed("Asian Cuisine", "Fried rice, chow mein, spring rolls, chicken teriyaki."),
            new CategorySeed("Salads & Healthy Options", "Fresh salads, fruit bowls, grilled proteins, wraps."),
            new CategorySeed("Bakery & Bread", "Bread, croissants, donuts, cakes, muffins."),
            new CategorySeed("Wine & Spirits", "Red wine, white wine, rosé, champagne, whiskey, vodka, gin."),
            new CategorySeed("Beer & Cider", "Local/imported beers, lagers, stouts, fruit ciders."),
            new CategorySeed("Cocktails & Mocktails", "Mojitos, margaritas, piña coladas, mocktails."),
            new CategorySeed("Hot Beverages", "Coffee, espresso, cappuccino, latte, tea, hot chocolate."),
            new CategorySeed("Appetizers", "Chicken wings, calamari, mozzarella sticks, nachos, loaded fries."),
            new CategorySeed("Shawarma & Wraps", "Chicken shawarma, beef shawarma, falafel wraps."),
            new CategorySeed("Barbecue & Grills", "BBQ ribs, grilled chicken, beef skewers, lamb chops.")
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
                    c.setStatus("AVAILABLE"); 
                    
                    Instant now = Instant.now();
                    c.setCreatedAt(now);
                    c.setUpdatedAt(now);
                    
                    return c;
                })
                .toList(); // Added this to finalize the Stream

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