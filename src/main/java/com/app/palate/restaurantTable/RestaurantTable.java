package com.app.palate.restaurantTable;

import java.util.ArrayList;
import java.util.List;

import com.app.palate.auth.Account;
import com.app.palate.tableAllocation.TableAllocation;
import com.app.palate.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RestaurantTable extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true)
    private String tableName;

    @NotNull
    @Column(nullable = false, unique = true)
    private Integer tableNumber;

    private Integer capacity;

    private String qrCode;

    @Enumerated(EnumType.STRING)
    private RestaurantTableStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiter_id", foreignKey = @ForeignKey(name = "fk_table_waiter"), nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Account waiter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", foreignKey = @ForeignKey(name = "fk_table_cashier"), nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Account cashier;

    @OneToMany(mappedBy = "table", fetch = FetchType.LAZY)
    private List<TableAllocation> allocations = new ArrayList<>();

}
