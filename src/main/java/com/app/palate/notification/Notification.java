package com.app.palate.notification;

import java.time.Instant;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.app.palate.auth.Account;
import com.app.palate.utils.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
@Table(name = "notifications")
public class Notification extends BaseEntity {

    private String title;

    private String type;

    private String message;

    @ManyToOne()
    @JoinColumn(name="account_id")
    private Account account;

    private Boolean isRead;

    private Instant readAt;
}
