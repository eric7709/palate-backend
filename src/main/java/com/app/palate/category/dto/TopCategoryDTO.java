package com.app.palate.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopCategoryDTO {
    private Long categoryId;
    private String name;
    private Double totalSales;
}
