package com.app.palate.order;

import java.util.ArrayList;
import java.util.List;
import com.app.palate.auth.Account;
import com.app.palate.customer.Customer;
import com.app.palate.orderItem.OrderItem;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.room.Room;
import com.app.palate.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_status_created", columnList = "status, createdAt")
})
public class Order extends BaseEntity {

    @Column(nullable = false)
    private double total;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private String invoiceNumber;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "waiter_id", foreignKey = @ForeignKey(name = "fk_order_waiter"), nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Account waiter;

    @ManyToOne
    @JoinColumn(name = "cashier_id", foreignKey = @ForeignKey(name = "fk_order_cashier"), nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Account cashier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", foreignKey = @ForeignKey(name = "fk_order_table"), nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private RestaurantTable table;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", foreignKey = @ForeignKey(name = "fk_order_room"), nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(name = "fk_order_customer"), nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Customer customer;

    @Column(nullable = true)
    private String virtualAccountNumber;

    private String monnifyReference;

    @Column(nullable = true)
    private String virtualBankName;
}
