package com.app.palate.category;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopCategoryDTO {
    private Long categoryId;
    private String name;
    private Double totalSales;
}
