package com.app.palate.dashboard.topCategories;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryItem {
  private int rank;
  private String name;
  private String value;
  private int pct;
  private String color;
}