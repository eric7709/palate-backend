package com.app.palate.category;

import java.util.List;

import com.app.palate.menuItem.MenuItem;
import com.app.palate.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@Entity
@Setter
@NoArgsConstructor
public class Category extends BaseEntity {

    @NotNull
    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "category")
    private List<MenuItem> menuItems;

    @Column(nullable = false)
    private String status;

    private String description;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = "ACTIVE";
        }
    }
}
