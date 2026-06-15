package com.app.palate.room;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.app.palate.auth.Account;
import com.app.palate.utils.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;



@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String roomNumber;

    private Integer floor;

    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "cashier_id",
        foreignKey = @ForeignKey(name = "fk_room_cashier")
    )
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Account cashier;
}