package com.app.palate.auth;

import com.app.palate.auth.enums.Gender;
import com.app.palate.auth.enums.AccountStatus;
import com.app.palate.auth.enums.Role;

import com.app.palate.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "accounts") // ✅ NOT "account"
@NoArgsConstructor
@Getter
@Setter
public class Account extends BaseEntity {

    @NotNull
    @Column(length = 255)

    @NotBlank
    private String firstName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull
    @Column(length = 255)

    @NotBlank
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    private String password;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Enumerated(EnumType.STRING)
    private Role role;

}
