package com.app.palate.menuItem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopMenuItemDTO {
    private Long menuItemId;
    private String name;
    private Double totalSales;
}
