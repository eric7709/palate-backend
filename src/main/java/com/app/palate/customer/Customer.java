package com.app.palate.customer;


import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.app.palate.utils.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "customer", schema = "public") // Verify if schema is public or something else
@NoArgsConstructor
@Entity
public class Customer extends BaseEntity {
    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private String title;

    private String phoneNumber;

    private String email;
}
