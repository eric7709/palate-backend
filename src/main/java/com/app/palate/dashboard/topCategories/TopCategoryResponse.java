package com.app.palate.dashboard.topCategories;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopCategoryResponse {
    private List<CategoryItem> items;
}