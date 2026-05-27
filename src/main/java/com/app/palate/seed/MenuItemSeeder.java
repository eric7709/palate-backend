package com.app.palate.seed;

import com.app.palate.category.Category;
import com.app.palate.category.CategoryRepository;
import com.app.palate.menuItem.MenuItem;
import com.app.palate.menuItem.MenuItemRepository;
import com.app.palate.menuItem.MenuItemStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class MenuItemSeeder {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

    private static final List<MenuItemSeed> MENU_ITEM_DATA = List.of(
            // Rice Dishes
            new MenuItemSeed("Jollof Rice", "Classic Nigerian Jollof rice", "Rice Dishes", 1500.0),
            new MenuItemSeed("Fried Rice", "Vegetable fried rice with chicken", "Rice Dishes", 1600.0),
            new MenuItemSeed("Coconut Rice", "Coconut-flavored rice", "Rice Dishes", 1700.0),
            new MenuItemSeed("Ofada Rice", "Traditional rice with spicy sauce", "Rice Dishes", 1800.0),
            new MenuItemSeed("Native Rice", "Local Nigerian rice variants", "Rice Dishes", 1500.0),

            // Swallows
            new MenuItemSeed("Pounded Yam", "Served with assorted soups", "Swallows", 1200.0),
            new MenuItemSeed("Eba", "Cassava flour swallow with soup", "Swallows", 1100.0),
            new MenuItemSeed("Fufu", "Soft yam or cassava swallow", "Swallows", 1100.0),
            new MenuItemSeed("Amala", "Yam flour swallow", "Swallows", 1150.0),
            new MenuItemSeed("Semovita", "Wheat swallow served with soup", "Swallows", 1100.0),

            // Soups & Stews
            new MenuItemSeed("Egusi Soup", "Melon seed soup with spinach and meat", "Soups & Stews", 2000.0),
            new MenuItemSeed("Ogbono Soup", "Thick ogbono seed soup", "Soups & Stews", 2000.0),
            new MenuItemSeed("Ewedu Soup", "Jute leaves soup, light and tasty", "Soups & Stews", 1800.0),
            new MenuItemSeed("Okra Soup", "Okra stew with assorted meat", "Soups & Stews", 1900.0),
            new MenuItemSeed("Banga Soup", "Palm nut soup with seafood", "Soups & Stews", 2200.0),
            new MenuItemSeed("Afang Soup", "Vegetable-rich soup", "Soups & Stews", 2000.0),
            new MenuItemSeed("Oha Soup", "Traditional oha leaf soup", "Soups & Stews", 2100.0),

            // Grilled & Fried Proteins
            new MenuItemSeed("Grilled Chicken", "Spicy grilled chicken", "Grilled & Fried Proteins", 2000.0),
            new MenuItemSeed("Peppered Chicken", "Hot peppered chicken", "Grilled & Fried Proteins", 2100.0),
            new MenuItemSeed("Asun", "Spicy grilled goat meat", "Grilled & Fried Proteins", 2500.0),
            new MenuItemSeed("Suya", "Classic Nigerian beef suya", "Grilled & Fried Proteins", 2200.0),
            new MenuItemSeed("Peppered Fish", "Grilled catfish with pepper sauce", "Grilled & Fried Proteins", 2300.0),

            // Snacks & Street Food
            new MenuItemSeed("Puff-Puff", "Fried dough balls", "Snacks & Street Food", 500.0),
            new MenuItemSeed("Meat Pie", "Savory meat-filled pastry", "Snacks & Street Food", 700.0),
            new MenuItemSeed("Akara", "Deep-fried bean cakes", "Snacks & Street Food", 600.0),
            new MenuItemSeed("Chin-Chin", "Sweet crunchy fried dough", "Snacks & Street Food", 500.0),
            new MenuItemSeed("Samosa", "Spicy vegetable or meat filling", "Snacks & Street Food", 600.0),
            new MenuItemSeed("Spring Rolls", "Crispy rolls with fillings", "Snacks & Street Food", 600.0),
            new MenuItemSeed("Plantain Chips", "Fried sweet plantains", "Snacks & Street Food", 400.0),

            // Beans & Legumes
            new MenuItemSeed("Moi Moi", "Steamed bean pudding", "Beans & Legumes", 1200.0),
            new MenuItemSeed("Ewa Agoyin", "Mashed beans in spicy sauce", "Beans & Legumes", 1300.0),
            new MenuItemSeed("Bean Porridge", "Hearty beans cooked with palm oil", "Beans & Legumes", 1400.0),
            new MenuItemSeed("Akara Snack", "Deep-fried bean balls", "Beans & Legumes", 600.0),

            // Breakfast Specials
            new MenuItemSeed("Yam Porridge", "Soft yam cooked in sauce", "Breakfast Specials", 1000.0),
            new MenuItemSeed("Akara & Bread", "Fried bean cakes with bread", "Breakfast Specials", 800.0),
            new MenuItemSeed("Pap (Ogi)", "Fermented cereal porridge", "Breakfast Specials", 700.0),
            new MenuItemSeed("Fried Plantains", "Sweet fried plantains", "Breakfast Specials", 600.0),
            new MenuItemSeed("Bread & Egg", "Simple breakfast combo", "Breakfast Specials", 800.0),
            new MenuItemSeed("Pancakes", "Fluffy pancakes with syrup", "Breakfast Specials", 900.0),

            // Drinks & Beverages
            new MenuItemSeed("Zobo", "Hibiscus drink", "Drinks & Beverages", 500.0),
            new MenuItemSeed("Kunu", "Millet-based drink", "Drinks & Beverages", 400.0),
            new MenuItemSeed("Chapman", "Non-alcoholic cocktail", "Drinks & Beverages", 600.0),
            new MenuItemSeed("Fresh Juice", "Seasonal fruit juice", "Drinks & Beverages", 700.0),
            new MenuItemSeed("Smoothie", "Blended fruit smoothie", "Drinks & Beverages", 900.0),
            new MenuItemSeed("Milkshake", "Vanilla, chocolate or strawberry", "Drinks & Beverages", 1000.0),
            new MenuItemSeed("Soft Drink", "Coke, Fanta, Sprite", "Drinks & Beverages", 500.0),

            // Desserts & Pastries
            new MenuItemSeed("Chin-Chin Sweet", "Crunchy fried dough", "Desserts & Pastries", 500.0),
            new MenuItemSeed("Puff-Puff Dessert", "Sweet fried dough balls", "Desserts & Pastries", 500.0),
            new MenuItemSeed("Coconut Candy", "Sweet coconut treat", "Desserts & Pastries", 400.0),
            new MenuItemSeed("Doughnut", "Glazed doughnut", "Desserts & Pastries", 600.0),
            new MenuItemSeed("Nigerian Sponge Cake", "Soft cake slice", "Desserts & Pastries", 700.0),
            new MenuItemSeed("Ice Cream", "Vanilla, chocolate, strawberry", "Desserts & Pastries", 900.0),
            new MenuItemSeed("Fruit Salad", "Mixed fresh fruits", "Desserts & Pastries", 800.0),

            // Seafood
            new MenuItemSeed("Peppered Prawns", "Spicy prawns", "Seafood", 2500.0),
            new MenuItemSeed("Catfish Pepper Soup", "Hot catfish broth", "Seafood", 2200.0),
            new MenuItemSeed("Grilled Tilapia", "Tilapia grilled with spices", "Seafood", 2300.0),
            new MenuItemSeed("Crab Dish", "Steamed crab with sauce", "Seafood", 2800.0),
            new MenuItemSeed("Periwinkle Sauce", "Local seafood delicacy", "Seafood", 2000.0),

            // Pepper Soups
            new MenuItemSeed("Goat Meat Pepper Soup", "Spicy aromatic goat soup", "Pepper Soups", 2200.0),
            new MenuItemSeed("Chicken Pepper Soup", "Hot chicken broth", "Pepper Soups", 2000.0),
            new MenuItemSeed("Fish Pepper Soup", "Fresh fish in spicy broth", "Pepper Soups", 2100.0),
            new MenuItemSeed("Point-and-Kill Pepper Soup", "Catch-of-the-day soup", "Pepper Soups", 2500.0),

            // Pasta & Noodles
            new MenuItemSeed("Spaghetti Jollof", "Spaghetti with tomato sauce", "Pasta & Noodles", 1400.0),
            new MenuItemSeed("Macaroni", "Baked macaroni with cheese", "Pasta & Noodles", 1500.0),
            new MenuItemSeed("Indomie", "Instant noodles with toppings", "Pasta & Noodles", 900.0),
            new MenuItemSeed("Yam & Egg Noodles", "Fried noodles with yam and egg", "Pasta & Noodles", 1200.0),

            // Porridges
            new MenuItemSeed("Yam Porridge", "Soft yam in sauce", "Porridges", 1000.0),
            new MenuItemSeed("Plantain Porridge", "Mashed plantain porridge", "Porridges", 1000.0),
            new MenuItemSeed("Beans Porridge", "Hearty beans cooked", "Porridges", 1200.0),
            new MenuItemSeed("Corn Porridge (Ogi)", "Fermented corn porridge", "Porridges", 800.0),

            // Side Dishes
            new MenuItemSeed("Fried Plantains (Dodo)", "Sweet fried plantain", "Side Dishes", 600.0),
            new MenuItemSeed("Coleslaw", "Shredded vegetables salad", "Side Dishes", 500.0),
            new MenuItemSeed("Steamed Vegetables", "Mixed veggies", "Side Dishes", 600.0),
            new MenuItemSeed("Garden Egg Sauce", "Sauce made from garden egg", "Side Dishes", 700.0),
            new MenuItemSeed("Ata Dindin", "Spicy tomato sauce", "Side Dishes", 700.0),

            // Fast Food & Continental
            new MenuItemSeed("Pizza Margherita", "Classic cheese pizza", "Pizza", 2500.0),
            new MenuItemSeed("Pepperoni Pizza", "Pizza with pepperoni", "Pizza", 2800.0),
            new MenuItemSeed("Beef Burger", "Juicy beef burger", "Burgers & Sandwiches", 2000.0),
            new MenuItemSeed("Chicken Burger", "Juicy chicken burger", "Burgers & Sandwiches", 1900.0),
            new MenuItemSeed("Club Sandwich", "Multi-layer sandwich", "Burgers & Sandwiches", 1800.0),
            new MenuItemSeed("French Fries", "Crispy fried potatoes", "Fast Food", 700.0),
            new MenuItemSeed("Fried Chicken", "Crispy fried chicken", "Fast Food", 2200.0),
            new MenuItemSeed("Chicken Wings", "Spicy chicken wings", "Fast Food", 1800.0),
            new MenuItemSeed("Lasagna", "Cheesy baked pasta", "Continental Dishes", 2500.0),
            new MenuItemSeed("Grilled Steak", "Medium-rare steak", "Continental Dishes", 3500.0),
            new MenuItemSeed("Fish & Chips", "Battered fish with fries", "Continental Dishes", 2300.0),

            // Asian Cuisine
            new MenuItemSeed("Chicken Teriyaki", "Grilled chicken in teriyaki sauce", "Asian Cuisine", 2000.0),
            new MenuItemSeed("Chow Mein", "Stir-fried noodles", "Asian Cuisine", 1500.0),
            new MenuItemSeed("Fried Rice Asian Style", "Asian fried rice", "Asian Cuisine", 1600.0),

            // Salads & Healthy Options
            new MenuItemSeed("Caesar Salad", "Classic Caesar salad", "Salads & Healthy Options", 1200.0),
            new MenuItemSeed("Fruit Bowl", "Fresh seasonal fruits", "Salads & Healthy Options", 900.0),
            new MenuItemSeed("Grilled Veggies", "Healthy grilled vegetables", "Salads & Healthy Options", 1000.0),

            // Drinks & Alcohol
            new MenuItemSeed("Red Wine", "Premium red wine", "Wine & Spirits", 5000.0),
            new MenuItemSeed("White Wine", "Chilled white wine", "Wine & Spirits", 5000.0),
            new MenuItemSeed("Beer", "Local or imported beer", "Beer & Cider", 800.0),
            new MenuItemSeed("Mojito", "Refreshing cocktail", "Cocktails & Mocktails", 1500.0),
            new MenuItemSeed("Coffee", "Freshly brewed coffee", "Hot Beverages", 700.0),
            new MenuItemSeed("Espresso", "Strong espresso shot", "Hot Beverages", 600.0));

    @Bean
    @Order(3)
    @Transactional
    CommandLineRunner seedMenuItems() {
        return args -> {
            Map<String, Category> categories = categoryRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(c -> c.getName().toLowerCase(), c -> c));

            for (MenuItemSeed seed : MENU_ITEM_DATA) {
                if (menuItemRepository.existsByNameIgnoreCase(seed.name()))
                    continue;

                Category category = categories.get(seed.categoryName().toLowerCase());
                if (category == null) {
                    System.err.println("⚠️ Category not found for menu item: " + seed.name());
                    continue;
                }

                MenuItem item = new MenuItem();
                item.setName(seed.name());
                item.setDescription(seed.description());
                item.setPrice(seed.price());
                item.setStatus(MenuItemStatus.AVAILABLE);
                item.setCategory(category);

                // Fix: Manually set the timestamps to satisfy the NOT NULL constraint
                Instant now = Instant.now();
                item.setCreatedAt(now);
                item.setUpdatedAt(now);

                menuItemRepository.save(item);
                System.out.println("✅ MenuItem created: " + seed.name());
            }
        };
    }

    private record MenuItemSeed(String name, String description, String categoryName, Double price) {
    }
}
