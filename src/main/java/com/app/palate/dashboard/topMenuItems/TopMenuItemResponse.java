package com.app.palate.dashboard.topMenuItems;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopMenuItemResponse {
    private List<Item> items;
  
}