package com.app.palate.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.app.palate.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private Instant expiryDate;

    public VerificationToken(Account account, TokenType type, int expiryTimeInMinutes) {
        this.account = account;
        this.type = type;
        this.token = UUID.randomUUID().toString();
        this.expiryDate = Instant.now().plus(expiryTimeInMinutes, ChronoUnit.MINUTES);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }
}