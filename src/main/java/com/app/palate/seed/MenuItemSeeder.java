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

    private static final Map<String, String> CUSTOM_IMAGE_URLS = new HashMap<>();

    static {
        // ----- Breakfast -----
        CUSTOM_IMAGE_URLS.put("Nigerian Breakfast", "https://dano.com.ng/wp-content/uploads/2018/10/breakfast-4.png");
        CUSTOM_IMAGE_URLS.put("Classic English Breakfast", "https://noseychef.com/wp-content/uploads/2019/02/IMG_4386.jpg");
        CUSTOM_IMAGE_URLS.put("Safron Signature Breakfast", "https://images.getrecipekit.com/20220609170725-french-20toast.png?aspect_ratio=1:1&quality=90&");
        CUSTOM_IMAGE_URLS.put("Full Breakfast Buffet", "https://thumbs.dreamstime.com/b/breakfast-buffet-hearty-line-food-trays-serving-utensils-81354887.jpg");
        CUSTOM_IMAGE_URLS.put("Egg (Breakfast Extra)", "https://www.aberdeenskitchen.com/wp-content/uploads/2019/05/Avocado-Egg-Breakfast-Toast-FI-Thumbnail-1200X1200.jpg");
        CUSTOM_IMAGE_URLS.put("Bread Toasts", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT1lzgJA77c6-iIdmK0qwFiT-9ymgnzrJ1HSA&s");
        CUSTOM_IMAGE_URLS.put("French Toast", "https://food.fnr.sndimg.com/content/dam/images/food/fullset/2010/4/13/0/GC_alton-brown-french-toast_s4x3.jpg.rend.hgtvcom.1280.1280.suffix/1382539328476.webp");
        CUSTOM_IMAGE_URLS.put("Waffles", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcREL4QPVRWZQsT7rjtKzkWF6AF7uSneZHQG9A&s");
        CUSTOM_IMAGE_URLS.put("Strawberry Pancakes", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQXjac36yDFbJnre5GCiN2Uw2XO2HG6wlTAew&s");
        CUSTOM_IMAGE_URLS.put("Fluffy Pancakes", "https://food.fnr.sndimg.com/content/dam/images/food/fullset/2017/4/7/0/FNK_Fluffy-Japanese-Pancakes-H_s4x3.jpg.rend.hgtvcom.1280.960.suffix/1491598033896.webp");
        CUSTOM_IMAGE_URLS.put("Bacon (Breakfast Extra)", "https://itsnotcomplicatedrecipes.com/wp-content/uploads/2020/09/Bacon-Egg-Galettes-1.jpg");
        CUSTOM_IMAGE_URLS.put("Chicken Sausages (Breakfast Extra)", "https://onestophalal.com/cdn/shop/articles/chicken_breakfast_sausage_1200x.jpg?v=1714141558");
        CUSTOM_IMAGE_URLS.put("Yellow Pap", "https://stermart.com/wp-content/uploads/2021/12/Akamu-cover-pic.jpg");
        CUSTOM_IMAGE_URLS.put("Oats", "https://theforkedspoon.com/wp-content/uploads/2021/09/Oats-3.jpg");
        CUSTOM_IMAGE_URLS.put("Custard", "https://vintagekitchennotes.com/wp-content/uploads/2021/03/vanilla-custard-sauce-close-up.jpg");
        // Custard has no image – will be null

        // ----- Pastries & Snacks -----
        CUSTOM_IMAGE_URLS.put("Meat Pie", "https://www.fmnfoods.com/wp-content/uploads/2020/06/Meat-pie-600.jpg");
        CUSTOM_IMAGE_URLS.put("Chicken Pie", "https://www.chefadora.com/_next/image?url=https%3A%2F%2Fchefadora-production.s3.ap-southeast-2.amazonaws.com%2Fmedium_Screenshot_20240801_022317_a2ed2218db.png&w=3840&q=75");
        CUSTOM_IMAGE_URLS.put("Doughnut", "https://cookingwithclaudy.com/wp-content/uploads/2023/06/20230612_132643-scaled-1.jpg");
        CUSTOM_IMAGE_URLS.put("Sausages", "https://airfryereats.com/wp-content/uploads/2022/06/Sausage-in-Air-Fryer-Featured-Image.jpg");

        // ----- Appetizers -----
        CUSTOM_IMAGE_URLS.put("Chili Gizzard", "https://cookingwithclaudy.com/wp-content/uploads/2023/04/20230424104314_IMG_5118-1-1.jpg");
        CUSTOM_IMAGE_URLS.put("Chicken Wings", "https://lowcarbafrica.com/wp-content/uploads/2023/02/Spicy-Chicken-Wings-IG-1.jpg");
        CUSTOM_IMAGE_URLS.put("Peppered Snails (Igbin)", "https://lowcarbafrica.com/wp-content/uploads/2023/08/African-Snail-Recipe-Peppered-Snails-IG.jpg");
        CUSTOM_IMAGE_URLS.put("Prawns Tempura", "https://cdn11.bigcommerce.com/s-lz0jp6q42t/images/stencil/1280x1280/products/1361/2880/Tempura_Prawns__20247.1761227856.jpg?c=1");
        CUSTOM_IMAGE_URLS.put("Hummus", "https://www.attainable-sustainable.net/wp-content/uploads/2018/04/red-pepper-hummus.jpg");
        CUSTOM_IMAGE_URLS.put("Hummus - Opt for Beef", "https://forksandfoliage.com/wp-content/uploads/2023/03/hummus-with-meat-18.jpg");
        CUSTOM_IMAGE_URLS.put("Hummus - Opt for Chicken", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQAgzK_rrz4fbELdDwOFaAThvA-kbQfIEPfSA&s");
        CUSTOM_IMAGE_URLS.put("Spring Rolls (Vegetable)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSI00RCqlg6ZtluuSKp3pCFW9Y4l-kZ2mL7kg&s");
        CUSTOM_IMAGE_URLS.put("Spring Rolls (Chicken)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSp4t6_9tL9GqmwJxXvOlyb23kI1S9v2RIK9Q&s");

        // ----- The Safron Platters -----
        CUSTOM_IMAGE_URLS.put("Signature Platter", "https://www.thefreshfishshop.com/cdn/shop/files/smallindulgentfestive.jpg?v=1764162797&width=533");
        CUSTOM_IMAGE_URLS.put("Seafood Platter", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_lC0lD5a10D8uN7DoNT0t6I9fdD_4pG9a7g&s");
        CUSTOM_IMAGE_URLS.put("Mixed Platter", "https://img.magnific.com/free-photo/mixed-fried-meat-with-pomegranate-sauce_140725-3461.jpg?semt=ais_hybrid&w=740&q=80");

        // ----- Nigerian Classics -----
        CUSTOM_IMAGE_URLS.put("Safron Signature Native Fried Rice", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSTuHJdGlGu9hXZGR--drgThagS4h1eVUvkug&s");
        CUSTOM_IMAGE_URLS.put("Seafood Okro", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTyEdOHwUqBJzGzPyvf0-XpcxpIOPx0WgOvDw&s");
        CUSTOM_IMAGE_URLS.put("Assorted Meat (Protein)", "https://simshomekitchen.com/wp-content/uploads/2018/07/Nigerian-soup.jpg");
        CUSTOM_IMAGE_URLS.put("Peppered Croaker (Protein)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRKk7qRewFMn0A_PqtWpU9xyuAKFq4piz-Yuw&s");
        CUSTOM_IMAGE_URLS.put("Beef Sauce (Protein)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQPhjQa63x4HuN4zxbkTmRuM4FMCP8OYnchOQ&s");
        CUSTOM_IMAGE_URLS.put("Chicken Sauce (Protein)", "https://www.laurafuentes.com/wp-content/uploads/2025/01/cottage-chees-sauce-for-chicken-RC.jpg");
        CUSTOM_IMAGE_URLS.put("Dried Fish (Protein)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTNEagm7_D0tGbyFYNgMY1fOcUEwkUn-Oyf1w&s");
        CUSTOM_IMAGE_URLS.put("Goat Meat (Protein)", "https://atastykitchen.com/wp-content/uploads/2023/08/Indian-goat-curry-recipe-11.jpg");
        CUSTOM_IMAGE_URLS.put("Turkey Sauce (Protein)", "https://mallorythedietitian.com/wp-content/uploads/2024/06/turkey-taco-skillet-1200-2.jpg");
        CUSTOM_IMAGE_URLS.put("Snail (Protein)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRYETNPlZIvGJcw0r66_CIjoQawMFoAVS_LlQ&s");

        // ----- Sides (Extra) -----
        CUSTOM_IMAGE_URLS.put("Extra Protein - Beef, Goat or Chicken", "https://naijahome.food/wp-content/uploads/2025/11/Nigerian-Peppered-Goat-Meat-Asun-Protein-naija-home-food-Lagos-Abuja-personal-chef-Ama-Cuisine-Ecstasy-Foods.jpg");
        CUSTOM_IMAGE_URLS.put("Snail (Side)", "https://lowcarbafrica.com/wp-content/uploads/2023/08/African-Snail-Recipe-Peppered-Snails-IG.jpg");
        CUSTOM_IMAGE_URLS.put("Prawns (Side)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRxoAatx0RbDjI3dZUkIM4Y6HbC5COoqTzKxQ&s");
        CUSTOM_IMAGE_URLS.put("Fish (Side)", "https://lh3.googleusercontent.com/proxy/jcL-KndVOFd7gMjAPWKTerxauTvDQsSRvv8-_hLqOfDqozNDYoUs_DRMloVOaI1kNrgUTlRiqtjcfvVnOT5FMVRbBxrP4B3fnIQVoHbmAb1X6ZCQNeGQ_Am9Bm47krLAKauSedEZbkP0o6YZRcY4SXPUvZwwfmNKpxpjs1FUL5E6ehhRVQgdHoWKitFdO5j3HKdxvfNVg-9u");
        CUSTOM_IMAGE_URLS.put("Seasonal Vegetables", "https://media-production.lp-cdn.com/media/xp2464t3e5nawfvv8vsj35cf");
        CUSTOM_IMAGE_URLS.put("Yam Porridge", "https://terracubes.net/wp-content/uploads/2023/10/Yam-Porridge-700x400.png");
        CUSTOM_IMAGE_URLS.put("Yam Fries", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQUZhfIZ4UyUUtQuPraH5dFbYChJuZjw5pebw&s");
        CUSTOM_IMAGE_URLS.put("French Fries", "https://img.taste.com.au/WSZBT1fA/w1200-h900-cfill-q80/taste/2016/11/rachel-87711-2.jpeg");
        CUSTOM_IMAGE_URLS.put("Sweet Potato Fries", "https://www.kitchensanctuary.com/wp-content/uploads/2021/10/Sweet-Potato-Fries-square-FS-20.jpg");
        CUSTOM_IMAGE_URLS.put("Fried Plantain", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSRkD-oQ4qXatXaoq7KGJvWpo6HSzucFHZr7Q&s");
        CUSTOM_IMAGE_URLS.put("Coleslaw", "https://thesaltycooker.com/wp-content/uploads/2024/05/Coleslaw-BLOG-1024x1024.jpg");
        CUSTOM_IMAGE_URLS.put("Turkey (Side)", "https://thesaltycooker.com/wp-content/uploads/2024/05/Coleslaw-BLOG-1024x1024.jpg");
        CUSTOM_IMAGE_URLS.put("The Safron Smoky Jollof", "https://allure.vanguardngr.com/wp-content/uploads/2021/03/maxresdefault-2-1024x576.jpeg");
        CUSTOM_IMAGE_URLS.put("Fried Rice (Side)", "https://iamhomesteader.com/wp-content/uploads/2021/01/fried-rice-2.jpg");
        CUSTOM_IMAGE_URLS.put("Steamed Basmati Rice", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRa2z4bA47TKvnucJTtBYzZKCBGKQr30PvQpg&s");

        // ----- Soups -----
        CUSTOM_IMAGE_URLS.put("Cream of Chicken Soup", "https://www.licious.in/blog/wp-content/uploads/2020/12/Cream-of-chicken-Soup.jpg");
        CUSTOM_IMAGE_URLS.put("Chicken Pepper Soup", "https://cookingwithclaudy.com/wp-content/uploads/2023/02/Untitled-design.jpg");
        CUSTOM_IMAGE_URLS.put("Turkey Pepper Soup", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTpa4njHq8h0c3mChQRQIH9W6mOHlHcOEjC0w&s");
        CUSTOM_IMAGE_URLS.put("Goat Meat Pepper Soup", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSki1F3X7_XN8fYENX6JuqhKMbOFcYjSh-BPw&s");
        CUSTOM_IMAGE_URLS.put("Fish Pepper Soup", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTGrrX96UUHXFj-3JydKpultCwf_EWDqqnLfQ&s");
        CUSTOM_IMAGE_URLS.put("Assorted Meat Pepper Soup", "https://i.ytimg.com/vi/f061fu8V8rY/hq720.jpg?sqp=-oaymwEhCK4FEIIDSFryq4qpAxMIARUAAAAAGAElAADIQj0AgKJD&rs=AOn4CLDBS34_-AtEzgsVDh3w_52y1l18oQ");

        // ----- Sandwiches, Burgers & Wraps -----
        CUSTOM_IMAGE_URLS.put("Classic Club Sandwich", "https://themom100.com/wp-content/uploads/2024/04/classic-club-sandwich-150922-H.jpg");
        CUSTOM_IMAGE_URLS.put("Shawarma - Chicken", "https://foxeslovelemons.com/wp-content/uploads/2023/06/Chicken-Shawarma-8.jpg");
        CUSTOM_IMAGE_URLS.put("Shawarma - Beef", "https://www.preciouscore.com/wp-content/uploads/2024/05/Beef-Shawarma-Recipe.jpg");
        CUSTOM_IMAGE_URLS.put("Shawarma - Mixed Chicken & Beef", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTn1_yshbB-qOSCKcRw5HqnDOhAjNXyQk_TQA&s");
        CUSTOM_IMAGE_URLS.put("Healthy Club Sandwich", "https://www.healthyfood.com/wp-content/uploads/2016/11/Grilled-chicken-club-sandwich-with-kumara-wedges-1024x664.jpg");
        CUSTOM_IMAGE_URLS.put("Tuna Melt", "https://iowagirleats.com/wp-content/uploads/2020/03/Tuna-Melt-Sandwich-iowagirleats-NEW-Featured.jpg");
        CUSTOM_IMAGE_URLS.put("Classic Philly Cheese Steak Sandwich", "https://www.alphafoodie.com/wp-content/uploads/2022/09/philly-cheesesteak-1-of-1.jpeg");
        CUSTOM_IMAGE_URLS.put("Grilled Chicken Burger", "https://playswellwithbutter.com/wp-content/uploads/2022/06/Best-Ever-Grilled-Chicken-Burgers-18.jpg");
        CUSTOM_IMAGE_URLS.put("The Safron Special Burger", "https://www.thecookierookie.com/wp-content/uploads/2023/04/featured-stovetop-burgers-recipe.jpg");

        // ----- Salads -----
        CUSTOM_IMAGE_URLS.put("Caesar Salad - Opt Prawn", "https://cdn.loveandlemons.com/wp-content/uploads/2024/12/caesar-salad-500x500.jpg");
        CUSTOM_IMAGE_URLS.put("Caesar Salad", "https://vintagekitchennotes.com/wp-content/uploads/2021/03/vanilla-custard-sauce-close-up.jpg");
        CUSTOM_IMAGE_URLS.put("Greek Salad", "https://www.elizabethrider.com/wp-content/uploads/2025/12/Greek-Salad-ElizabethRider-4-1024x683.jpg");
        CUSTOM_IMAGE_URLS.put("Garden Salad", "https://zestfulkitchen.com/wp-content/uploads/2022/07/garden-salad_for-web-3-736x809.jpg");
        CUSTOM_IMAGE_URLS.put("Seafood Salad", "https://food.fnr.sndimg.com/content/dam/images/food/fullset/2013/4/5/4/FNM_050113-Seafood-Salad-Recipe_s4x3.jpg.rend.hgtvcom.1280.960.suffix/1371615839240.webp");
        CUSTOM_IMAGE_URLS.put("Avocado Salad", "https://cdn.loveandlemons.com/wp-content/uploads/2024/07/avocado-salad.jpg");

        // ----- Signature Main Course Dishes -----
        CUSTOM_IMAGE_URLS.put("Cordon Blue", "https://www.asweetpeachef.com/wp-content/uploads/2010/08/Chicken-Cordon-Bleu-facebook-1.jpg");
        CUSTOM_IMAGE_URLS.put("Grilled Croaker Fillet", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ9XTI1ot2dFBsc7I4NmJaPjlXKsVBSwg4RRVbKro7DWxYLCbAhhMF83kQ&s=10");
        CUSTOM_IMAGE_URLS.put("Salmon Fillet", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQBiNkda-miitIyOIoZTHZREeU01ez7h_o-NjpD926HeZyXmMh9EFSkuSzy&s=10");
        CUSTOM_IMAGE_URLS.put("Short Ribs", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQyZTHGTSgSzlGvmdH7D59N_lpa1C5xz84WVN_G5jCGf20xAAm4ShOL5IR9&s=10");
        CUSTOM_IMAGE_URLS.put("Prawn Thermidor", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTqe8VCITlQ6s9xZNh_GHtQvWfvZz2W7-pn2E6jBmf4ZG8w0RcWlZcVr3TG&s=10");
        CUSTOM_IMAGE_URLS.put("Roast Chicken - Half Roast", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT68vhNktvbVwGKFgS1dHgjCuv_6nfitpGPUxC_hoFdPzST96OdwGD2iTs&s=10");
        CUSTOM_IMAGE_URLS.put("Roast Chicken - Full Roast", "https://hips.hearstapps.com/hmg-prod/images/roast-chicken-recipe-2-66b231ac9a8fb.jpg?crop=0.6666666666666667xw:1xh;center,top&resize=1200:*");

        // ----- From The Grill -----
        CUSTOM_IMAGE_URLS.put("T-Bone Steak (Imported)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTaLAwV4H7O8SgIlG8nIugs3aKEAUhvpOd_TG6EOXGuNiOgOg2AJuxaVe0&s=10");
        CUSTOM_IMAGE_URLS.put("Grilled Ribeye (Imported)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTWQaODf8jZ6yl47G8h06RK-EDMqJIBDQssPqZiF_NpbhhY7K8e7YsMyQE&s=10");
        CUSTOM_IMAGE_URLS.put("Lamb Chops (Imported)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT1qT3ZBtc4zBGJFbV4xKJ9hpwB8MOaZF0SEINcMSFQxg&s=10");

        // ----- Pasta -----
        CUSTOM_IMAGE_URLS.put("Spaghetti Bolognese", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcREqf2Hs-vKYcx7LznHcB_HC0974Xg_ye5RkLJQvlG5XQcK6836_BVOMYQz&s=10");
        CUSTOM_IMAGE_URLS.put("Alfredo - Opt Chicken", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSiwpLQJP21SKe56X6YbGBGyCQERI-U8De1uLCA1ZPKL2I-qLQUk2LZ0FM&s=10");
        CUSTOM_IMAGE_URLS.put("Alfredo - Opt Prawn", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSYYRo-hebeew5IVDed7RMpDTQXg7PL7MxPvb1aaUtBxQ&s=10");
        CUSTOM_IMAGE_URLS.put("Arabiatta Pasta - Opt Chicken", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRuaRaANMMN001VeDGWr4ItRs9F3pu-w0UAyuOR7XoHZg&s=10");
        CUSTOM_IMAGE_URLS.put("Arabiatta Pasta - Opt Prawn", "https://hips.hearstapps.com/redonline/main/thumbs/22671/sirtfood-prawn-arrabbiata-redonline.co.uk.jpg");
        CUSTOM_IMAGE_URLS.put("Seafood Pasta", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS-UMNH_6f_E2J1EI_FqVcZ_IY2ZcpSQTJLlj0MTo9QdUe5VTXKxHbDl3Jw&s=10");
        CUSTOM_IMAGE_URLS.put("Fusilli Al Pesto - Opt Chicken", "https://easysavorymeals.com/wp-content/uploads/2025/11/homemade-Grilled-Chicken-Pesto-Pasta.jpg");
        CUSTOM_IMAGE_URLS.put("Fusilli Al Pesto - Opt Prawn", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQT74zL3dWJRR9guSccHv8MM6oRaazPaGeR8z4fP3ZlQhmKurSQ5DkWaZC9&s=10");

        // ----- Pizza -----
        CUSTOM_IMAGE_URLS.put("Chicken Pizza", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT6hPHw2APmxEJIs6Aj8MW5u3dgGp-Wm8XySdY9wMDmyDGI1eG7hRc0bxI&s=10");
        CUSTOM_IMAGE_URLS.put("Vegetable Pizza", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSsJ252PqaVTrIhzqxw2cfKT4Qo36MPdllxhdVGVHq8jLPaGyFm_mLLoPev&s=10");
        CUSTOM_IMAGE_URLS.put("Suya Pizza - Chicken", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRpz7Tg3crB6R6G1L7kAHU9jQlNG8h5aB5CNX_6giDKQ6qd_yc4I_QlRw&s=10");
        CUSTOM_IMAGE_URLS.put("Suya Pizza - Beef", "https://bobsdinerlos.com/wp-content/uploads/2025/10/Untitled-design-8.png");
        CUSTOM_IMAGE_URLS.put("Pepperoni Pizza", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRrXNDTAGPqH0dRVPV0wt2qdlgCK6KYHT_jDf6cwWeZNX_IfD2HfGVfpl4&s=10");
        CUSTOM_IMAGE_URLS.put("Extra Cheese (Pizza)", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSOb4Kphg9NsnYjvZsaQGtOiNXnm-zGvV172Yr_zQaMm1c25Gw-YbYR8yM&s=10");

        // ----- Dessert -----
        CUSTOM_IMAGE_URLS.put("Classic Cheesecake", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRIyhKmrfOyUT1y1FvhFIGGpBo4NEhN7PX3fxYP-zHDmaHtG_oIwKZOnUvv&s=10");
        // Ice Cream (Classic) – no image
        CUSTOM_IMAGE_URLS.put("Ice Cream (Classic)", "https://lakesicecream.com/wp-content/uploads/2020/06/lakes-classic-icecream-chocolate.jpg");
        CUSTOM_IMAGE_URLS.put("Ice Cream (Strawberry)", "https://5.imimg.com/data5/SELLER/Default/2023/1/ME/GN/NS/93794828/strawberry-ice-cream-cone.jpg");
        CUSTOM_IMAGE_URLS.put("Ice Cream (Vanilla)", "https://tarateaspoon.com/wp-content/uploads/2021/06/Vanilla-Ice-Cream-cone-glass-sq.jpeg");
        CUSTOM_IMAGE_URLS.put("Ice Cream (Chocolate)", "https://www.smalltownwoman.com/wp-content/uploads/2020/06/Chocolate-Ice-Cream-DSC_1175-scaled.jpg");
        CUSTOM_IMAGE_URLS.put("Mixed Seasonal Fruit", "https://static.vecteezy.com/system/resources/previews/054/123/442/non_2x/fresh-mixed-fruit-bowl-showcasing-vibrant-and-colorful-seasonal-fruits-png.png");
        CUSTOM_IMAGE_URLS.put("Special Safron Brownie", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS7YC1kPkRi3DjAaRY25hidHF-il0CjdXkGpqOwAOUKU29Q_aMPi1037QXv&s=10");
        CUSTOM_IMAGE_URLS.put("Event Cake - Red Velvet", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTz6Ecs4xtAiw3FP16XH8qSOMpnDqWId3qgZdFYHLkD7VsKE78u111janFl&s=10");
        CUSTOM_IMAGE_URLS.put("Event Cake - Chocolate", "https://crumblesofhealth.com/wp-content/uploads/2022/02/Chocolate-mini-cakes12.jpeg");
        CUSTOM_IMAGE_URLS.put("Event Cake - Cream Caramel", "https://www.rockrecipes.com/wp-content/uploads/2013/03/The-Best-Caramel-Cake-slice-of-cake-on-a-white-plate-with-coffee-in-background.jpg");
    }

    private static final List<MenuItemSeed> MENU_ITEM_DATA = List.of(

            // Breakfast
            new MenuItemSeed("Nigerian Breakfast", "Your Choice of Sauce (Egg Sauce, Fish Sauce, Chicken Sauce), with Yam/Plantain (Fried or Boiled).", "Breakfast", 25000.0),
            new MenuItemSeed("Classic English Breakfast", "Your Choice of Eggs, Bacon, Chicken Sausage, Baked Beans, Grilled Tomatoes, Mushroom, Bread Toast.", "Breakfast", 25000.0),
            new MenuItemSeed("Safron Signature Breakfast", "Choice of Eggs, French Toast or Waffle or Pancakes, Bacon, Chicken Sausage, Whipped Cream & Mixed Berries, Maple Syrup or Chocolate Syrup.", "Breakfast", 25000.0),
            new MenuItemSeed("Full Breakfast Buffet", "Our Full Selection of Hot and Cold Items, served in the Restaurant from the Buffet. 6AM-10AM Weekdays | 6AM-11AM Sat & Sun.", "Breakfast", 30000.0),
            new MenuItemSeed("Egg (Breakfast Extra)", "Your choice of eggs.", "Breakfast", 3000.0),
            new MenuItemSeed("Bread Toasts", "Breakfast extra.", "Breakfast", 3000.0),
            new MenuItemSeed("French Toast", "Breakfast extra.", "Breakfast", 10000.0),
            new MenuItemSeed("Waffles", "Breakfast extra.", "Breakfast", 15000.0),
            new MenuItemSeed("Strawberry Pancakes", "Breakfast extra.", "Breakfast", 10000.0),
            new MenuItemSeed("Fluffy Pancakes", "Breakfast extra.", "Breakfast", 10000.0),
            new MenuItemSeed("Bacon (Breakfast Extra)", "Breakfast extra.", "Breakfast", 5000.0),
            new MenuItemSeed("Chicken Sausages (Breakfast Extra)", "Breakfast extra.", "Breakfast", 8000.0),
            new MenuItemSeed("Yellow Pap", "Breakfast extra.", "Breakfast", 4000.0),
            new MenuItemSeed("Oats", "Breakfast extra.", "Breakfast", 5000.0),
            new MenuItemSeed("Custard", "Breakfast extra.", "Breakfast", 5000.0),

            // Pastries & Snacks
            new MenuItemSeed("Meat Pie", "Savory meat-filled pastry.", "Pastries & Snacks", 3500.0),
            new MenuItemSeed("Chicken Pie", "Savory chicken-filled pastry.", "Pastries & Snacks", 4000.0),
            new MenuItemSeed("Doughnut", "Classic doughnut.", "Pastries & Snacks", 2000.0),
            new MenuItemSeed("Sausages", "Pastries & snacks selection.", "Pastries & Snacks", 3000.0),

            // Appetizers
            new MenuItemSeed("Chili Gizzard", "Tender Fried Gizzard in a Creamy Chili Sauce.", "Appetizers", 15000.0),
            new MenuItemSeed("Chicken Wings", "Your Choice of Sauce (Crispy Plain, Chili, Bbq, Suya Spice).", "Appetizers", 18000.0),
            new MenuItemSeed("Peppered Snails (Igbin)", "A Tasty Plate of Spicy Peppered Nigerian Snails Sauteed with Bell Peppers, Local Spices.", "Appetizers", 35000.0),
            new MenuItemSeed("Prawns Tempura", "Crispy Tiger Prawn with Lemon Wedges Served with Sweet Chili.", "Appetizers", 35000.0),
            new MenuItemSeed("Hummus", "Traditional Hummus, Pita Bread and a Choice of Topping.", "Appetizers", 25000.0),
            new MenuItemSeed("Hummus - Opt for Beef", "Hummus with beef topping option.", "Appetizers", 35000.0),
            new MenuItemSeed("Hummus - Opt for Chicken", "Hummus with chicken topping option.", "Appetizers", 30000.0),
            new MenuItemSeed("Spring Rolls (Vegetable)", "Spring Rolls Served with Sweet Chili Sauce - Vegetable option.", "Appetizers", 12000.0),
            new MenuItemSeed("Spring Rolls (Chicken)", "Spring Rolls Served with Sweet Chili Sauce - Chicken option.", "Appetizers", 15000.0),

            // The Safron Platters
            new MenuItemSeed("Signature Platter", "An Elaborate Platter of Chicken Spring Roll, Grilled Beef Suya, Chicken Wings, Peppered Snails, Potato Wedges, Sweet Potato Fries Coleslaw, Butter Corn, Bam Bam Chili Sauce.", "The Safron Platters", 80000.0),
            new MenuItemSeed("Seafood Platter", "Grilled Prawns, Fish Fillet, Crabs, Crispy Calamari, Skewered Shrimps French Fries, Ketchup, Chili Sauce, Tartar Sauce.", "The Safron Platters", 90000.0),
            new MenuItemSeed("Mixed Platter", "A Great Combination of Chicken Spring Roll, Chicken Wings, Pepper Gizzard, Grilled Prawns, Spicy Snails, Yam Fries, French Fries, Coleslaw, Ketchup, Chili Sauce.", "The Safron Platters", 120000.0),

            // Nigerian Classics
            new MenuItemSeed("Safron Signature Native Fried Rice", "Comes with your choice of protein and side.", "Nigerian Classics", 25000.0),
            new MenuItemSeed("Seafood Okro", "Nigerian classic seafood okro stew.", "Nigerian Classics", 35000.0),
            new MenuItemSeed("Assorted Meat (Protein)", "Protein option for soups and stews.", "Nigerian Classics", 20000.0),
            new MenuItemSeed("Peppered Croaker (Protein)", "Protein option for soups and stews.", "Nigerian Classics", 34000.0),
            new MenuItemSeed("Beef Sauce (Protein)", "Protein option for soups and stews.", "Nigerian Classics", 20000.0),
            new MenuItemSeed("Chicken Sauce (Protein)", "Protein option for soups and stews.", "Nigerian Classics", 20000.0),
            new MenuItemSeed("Dried Fish (Protein)", "Protein option for soups and stews.", "Nigerian Classics", 20000.0),
            new MenuItemSeed("Goat Meat (Protein)", "Protein option for soups and stews.", "Nigerian Classics", 25000.0),
            new MenuItemSeed("Turkey Sauce (Protein)", "Protein option for soups and stews.", "Nigerian Classics", 22000.0),
            new MenuItemSeed("Snail (Protein)", "Protein option for soups and stews.", "Nigerian Classics", 35000.0),

            // Sides (Extra)
            new MenuItemSeed("Extra Protein - Beef, Goat or Chicken", "Choice of extra protein side.", "Sides (Extra)", 15000.0),
            new MenuItemSeed("Snail (Side)", "Extra side option.", "Sides (Extra)", 35000.0),
            new MenuItemSeed("Prawns (Side)", "Extra side option.", "Sides (Extra)", 28000.0),
            new MenuItemSeed("Fish (Side)", "Extra side option.", "Sides (Extra)", 25000.0),
            new MenuItemSeed("Seasonal Vegetables", "Extra side option.", "Sides (Extra)", 15000.0),
            new MenuItemSeed("Yam Porridge", "Extra side option.", "Sides (Extra)", 15000.0),
            new MenuItemSeed("Yam Fries", "Extra side option.", "Sides (Extra)", 8000.0),
            new MenuItemSeed("French Fries", "Extra side option.", "Sides (Extra)", 8000.0),
            new MenuItemSeed("Sweet Potato Fries", "Extra side option.", "Sides (Extra)", 8000.0),
            new MenuItemSeed("Fried Plantain", "Extra side option.", "Sides (Extra)", 8000.0),
            new MenuItemSeed("Coleslaw", "Extra side option.", "Sides (Extra)", 8000.0),
            new MenuItemSeed("Turkey (Side)", "Extra side option.", "Sides (Extra)", 20000.0),
            new MenuItemSeed("The Safron Smoky Jollof", "Extra side option.", "Sides (Extra)", 10000.0),
            new MenuItemSeed("Fried Rice (Side)", "Extra side option.", "Sides (Extra)", 10000.0),
            new MenuItemSeed("Steamed Basmati Rice", "Extra side option.", "Sides (Extra)", 8000.0),

            // Soups
            new MenuItemSeed("Cream of Chicken Soup", "A Delightful Bowl of Delicious, Creamy Chicken Soup.", "Soups", 20000.0),
            new MenuItemSeed("Chicken Pepper Soup", "Spicy chicken pepper soup.", "Soups", 20000.0),
            new MenuItemSeed("Turkey Pepper Soup", "Spicy turkey pepper soup.", "Soups", 25000.0),
            new MenuItemSeed("Goat Meat Pepper Soup", "Spicy goat meat pepper soup.", "Soups", 25000.0),
            new MenuItemSeed("Fish Pepper Soup", "Spicy fish pepper soup.", "Soups", 20000.0),
            new MenuItemSeed("Assorted Meat Pepper Soup", "Spicy assorted meat pepper soup.", "Soups", 20000.0),

            // Sandwiches, Burgers & Wraps
            new MenuItemSeed("Classic Club Sandwich", "Grilled Chicken Breast, Lettuce, Tomato Slices, Egg and Bacon Topped, Melted Cheese, French Fries, Mayo Sauce.", "Sandwiches, Burgers & Wraps", 20000.0),
            new MenuItemSeed("Shawarma - Chicken", "Lebanese bread with Garlic Sauce, Pickles, and Lettuce - Chicken.", "Sandwiches, Burgers & Wraps", 16000.0),
            new MenuItemSeed("Shawarma - Beef", "Lebanese bread with Garlic Sauce, Pickles, and Lettuce - Beef.", "Sandwiches, Burgers & Wraps", 18000.0),
            new MenuItemSeed("Shawarma - Mixed Chicken & Beef", "Lebanese bread with Garlic Sauce, Pickles, and Lettuce - Mixed Chicken & Beef.", "Sandwiches, Burgers & Wraps", 20000.0),
            new MenuItemSeed("Healthy Club Sandwich", "Low Fat / Low Cholesterol. Tuna Mixed with Egg White, Lettuce, Sweet Corn, and Tomato on Whole Wheat Bread.", "Sandwiches, Burgers & Wraps", 18000.0),
            new MenuItemSeed("Tuna Melt", "Oil-Braised Tuna Mayonnaise Sandwich Topped with Melted Cheese, Served with French Fries.", "Sandwiches, Burgers & Wraps", 25000.0),
            new MenuItemSeed("Classic Philly Cheese Steak Sandwich", "Grilled Fillet Steak with Savory Grilled Green Peppers, Juicy Mushroom and Caramelized Onions Topped with Melted Cheese, Tomato, Shredded Lettuce, Pickle, French Fries.", "Sandwiches, Burgers & Wraps", 25000.0),
            new MenuItemSeed("Grilled Chicken Burger", "Grilled Chicken Breast, Lettuce, Fresh Tomato Slice, Red Onion, Cheddar Cheese, Sriracha Mayo, French Fries.", "Sandwiches, Burgers & Wraps", 20000.0),
            new MenuItemSeed("The Safron Special Burger", "Our Signature Beef Patty, Lettuce, Tomato, Cheddar Cheese and Onions Served with Cocktail Sauce.", "Sandwiches, Burgers & Wraps", 22000.0),

            // Salads
            new MenuItemSeed("Caesar Salad", "Sliced with Fresh Iceberg Lettuce, anchovy, croutons, Tossed in our Homemade Caesar Dressing.", "Salads", 22000.0),
            new MenuItemSeed("Caesar Salad - Opt Prawn", "Caesar Salad with prawn option.", "Salads", 28000.0),
            new MenuItemSeed("Greek Salad", "Fresh Lettuce, Tomato, Cucumber, Feta Cheese, Green Pepper, Black and Green Olive, Onion, and Virgin Olive Oil in Vinaigrette Dressing.", "Salads", 20000.0),
            new MenuItemSeed("Garden Salad", "Fresh Salad Greens, Garnished with Fresh Tomato, Cucumber, Lettuce, Green Bell Pepper, Carrot, Cabbage, Served with Cocktail Dressing.", "Salads", 20000.0),
            new MenuItemSeed("Seafood Salad", "Fresh Lettuce, Tomato, Cucumber, Green Pepper, Calamari, Shrimps, and Onion in a Mustard Vinaigrette Olive Oil Dressing.", "Salads", 25000.0),
            new MenuItemSeed("Avocado Salad", "Romaine Lettuce Salad Paired with Parmesan Cheese, Seared Chicken Breast, Cucumber and Classic Caesar Dressing.", "Salads", 20000.0),

            // Signature Main Course Dishes
            new MenuItemSeed("Cordon Blue", "Marinated Chicken Breast Stuffed with Ham and Cheese, in a Crispy Flour, Egg Batter, Deep Fried and Served with a Choice of Fries, Rice, Or Mashed Potato.", "Signature Main Course Dishes", 18000.0),
            new MenuItemSeed("Grilled Croaker Fillet", "Grilled Croaker Fish Fillet with your Choice of Steamed, Fried or Jollof Rice and Sauteed Vegetables.", "Signature Main Course Dishes", 35000.0),
            new MenuItemSeed("Salmon Fillet", "250g Fresh Salmon Fillet, Sauteed Vegetables or mashed Potato served with Lemon Butter Sauce and Fresh Parsley.", "Signature Main Course Dishes", 45000.0),
            new MenuItemSeed("Short Ribs", "Slow Braised Bbq Short Ribs, Creamy Mash Potatoes, Mixed Vegetables.", "Signature Main Course Dishes", 55000.0),
            new MenuItemSeed("Prawn Thermidor", "Grilled Prawn, Sprinkled with Cheese in a White Sauce with your Choice of Side.", "Signature Main Course Dishes", 45000.0),
            new MenuItemSeed("Roast Chicken - Half Roast", "Grilled Chicken, Chili Oil, Chicken Gravy, Sauteed Vegetables, with a side of Rice - Half Roast.", "Signature Main Course Dishes", 40000.0),
            new MenuItemSeed("Roast Chicken - Full Roast", "Grilled Chicken, Chili Oil, Chicken Gravy, Sauteed Vegetables, with a side of Rice - Full Roast.", "Signature Main Course Dishes", 50000.0),

            // From The Grill
            new MenuItemSeed("T-Bone Steak (Imported)", "400g Imported Beef Served with Your Choice of Sides.", "From The Grill", 60000.0),
            new MenuItemSeed("Grilled Ribeye (Imported)", "Ribeye Grilled to Perfection with your Choice of Sides.", "From The Grill", 45000.0),
            new MenuItemSeed("Lamb Chops (Imported)", "Grilled Lamb Chops, with a Choice of Sides.", "From The Grill", 45000.0),

            // Pasta
            new MenuItemSeed("Spaghetti Bolognese", "Minced beef meat in an Italian tomato sauce, topped with parmesan cheese.", "Pasta", 25000.0),
            new MenuItemSeed("Alfredo - Opt Chicken", "Chicken and mushroom in a piquant white sauce, topped with parmesan cheese - Chicken option.", "Pasta", 25000.0),
            new MenuItemSeed("Alfredo - Opt Prawn", "Chicken and mushroom in a piquant white sauce, topped with parmesan cheese - Prawn option.", "Pasta", 28000.0),
            new MenuItemSeed("Arabiatta Pasta - Opt Chicken", "Sweet spicy Italian tomato sauce, garlic, and herb with choice of chicken - Chicken option.", "Pasta", 25000.0),
            new MenuItemSeed("Arabiatta Pasta - Opt Prawn", "Sweet spicy Italian tomato sauce, garlic, and herb with choice of shrimps - Prawn option.", "Pasta", 28000.0),
            new MenuItemSeed("Seafood Pasta", "Mixed seafood in a bright and fragrant Italian tomato sauce.", "Pasta", 35000.0),
            new MenuItemSeed("Fusilli Al Pesto - Opt Chicken", "Fusilli pasta tossed with pesto sauce, topped with parmesan cheese - Chicken option.", "Pasta", 25000.0),
            new MenuItemSeed("Fusilli Al Pesto - Opt Prawn", "Fusilli pasta tossed with pesto sauce, topped with parmesan cheese - Prawn option.", "Pasta", 28000.0),

            // Pizza
            new MenuItemSeed("Chicken Pizza", "Chicken, green and red bell peppers with sweet corn.", "Pizza", 18000.0),
            new MenuItemSeed("Vegetable Pizza", "Caramelized onions, oregano, green and fresh bell peppers with sweet corn, sliced tomato and mushroom, (can be made vegan without cheese).", "Pizza", 17000.0),
            new MenuItemSeed("Suya Pizza - Chicken", "Beef Suya, Onions, Green Chili, Oregano, Topped with Suya Spice - Chicken option.", "Pizza", 20000.0),
            new MenuItemSeed("Suya Pizza - Beef", "Beef Suya, Onions, Green Chili, Oregano, Topped with Suya Spice - Beef option.", "Pizza", 20000.0),
            new MenuItemSeed("Pepperoni Pizza", "Pepperoni slices, on our traditional base.", "Pizza", 20000.0),
            new MenuItemSeed("Extra Cheese (Pizza)", "Add extra cheese to any pizza.", "Pizza", 5000.0),

            // Dessert – split ice creams
            new MenuItemSeed("Classic Cheesecake", "Classic cheesecake slice.", "Dessert", 15000.0),
            new MenuItemSeed("Ice Cream (Classic)", "Smooth and creamy classic ice cream.", "Dessert", 12000.0),
            new MenuItemSeed("Ice Cream (Strawberry)", "Sweet strawberry flavored ice cream.", "Dessert", 12000.0),
            new MenuItemSeed("Ice Cream (Vanilla)", "Rich vanilla bean ice cream.", "Dessert", 12000.0),
            new MenuItemSeed("Ice Cream (Chocolate)", "Decadent chocolate ice cream.", "Dessert", 12000.0),
            new MenuItemSeed("Mixed Seasonal Fruit", "A selection of mixed seasonal fruit.", "Dessert", 12000.0),
            new MenuItemSeed("Special Safron Brownie", "Our signature brownie dessert.", "Dessert", 17000.0),
            new MenuItemSeed("Event Cake - Red Velvet", "A Slice of Cake for your celebration - Red Velvet Cake.", "Dessert", 15000.0),
            new MenuItemSeed("Event Cake - Chocolate", "A Slice of Cake for your celebration - Chocolate Cake.", "Dessert", 15000.0),
            new MenuItemSeed("Event Cake - Cream Caramel", "A Slice of Cake for your celebration - Cream Caramel Cake.", "Dessert", 15000.0)
    );

    @Bean
    @Order(4)
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
                item.setImageUrl(CUSTOM_IMAGE_URLS.get(seed.name())); // null if missing

                Instant now = Instant.now();
                item.setCreatedAt(now);
                item.setUpdatedAt(now);

                menuItemRepository.save(item);
                System.out.println("✅ MenuItem created: " + seed.name());
            }
        };
    }

    private record MenuItemSeed(String name, String description, String categoryName, Double price) {}
}