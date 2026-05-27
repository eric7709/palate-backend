package com.app.palate.tableAllocation;

import com.app.palate.auth.Account;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TableAllocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", foreignKey = @ForeignKey(name = "fk_allocation_table"), nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private RestaurantTable table;

    // Cashier
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id")
    private Account cashier;

    @Column(name = "cashier_allocated_at")
    private Instant cashierAllocatedAt;

    @Column(name = "cashier_deallocated_at")
    private Instant cashierDeallocatedAt;

    // Waiter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiter_id")
    private Account waiter;

    @Column(name = "waiter_allocated_at")
    private Instant waiterAllocatedAt;

    @Column(name = "waiter_deallocated_at")
    private Instant waiterDeallocatedAt;
}