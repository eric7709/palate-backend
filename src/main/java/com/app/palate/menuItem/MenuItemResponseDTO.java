package com.app.palate.menuItem;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemResponseDTO {
        private Long id;
        private String name;
        private Double price;
        private String description;
        private MenuItemStatus status;
        private String imageUrl;
        private Long categoryId;
        private String categoryName;
}