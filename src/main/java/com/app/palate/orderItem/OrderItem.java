package com.app.palate.orderItem;

import com.app.palate.menuItem.MenuItem;
import com.app.palate.order.Order;
import com.app.palate.utils.BaseEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class OrderItem extends BaseEntity {
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "fk_orderitem_order"))
    private Order order;

    @ManyToOne
    @JoinColumn(name = "menu_item_id", foreignKey = @ForeignKey(name = "fk_orderitem_menuitem"))
    @OnDelete(action = OnDeleteAction.SET_NULL) // ✅ Add this
    private MenuItem menuItem;

    @Column(nullable = false)
    private boolean takeOut;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double price;

}
