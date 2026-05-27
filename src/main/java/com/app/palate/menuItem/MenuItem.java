    package com.app.palate.menuItem;

    import org.hibernate.annotations.OnDelete;
    import org.hibernate.annotations.OnDeleteAction;
    import org.springframework.data.jpa.domain.support.AuditingEntityListener;
    import com.app.palate.category.Category;
    import com.app.palate.utils.BaseEntity;
    import jakarta.persistence.*;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import lombok.*;

    @Getter
    @Setter
    @Entity
    @NoArgsConstructor
    @EntityListeners(AuditingEntityListener.class)
    public class MenuItem extends BaseEntity {

        @NotBlank
        @Column(nullable = false)
        private String name;

        @NotBlank
        @Column(nullable = false)
        private String description;

        @NotNull
        @Column(nullable = false)
        private Double price;

        @Column(nullable = false)
        private MenuItemStatus status;

        private String imageUrl;

        @ManyToOne
        @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_menuitem_category"))
        @OnDelete(action = OnDeleteAction.SET_NULL)
        private Category category;
    }
