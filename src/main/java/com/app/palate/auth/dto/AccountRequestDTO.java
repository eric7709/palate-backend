package com.app.palate.auth.dto;

import com.app.palate.auth.enums.AccountStatus;
import com.app.palate.auth.enums.Gender;
import com.app.palate.auth.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Gender gender;
    private String password;
    private AccountStatus status;
    private Role role;
}
