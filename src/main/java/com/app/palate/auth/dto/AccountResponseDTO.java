package com.app.palate.auth.dto;

import com.app.palate.auth.enums.Gender;
import com.app.palate.auth.enums.Role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;
    private String phoneNumber;
    private Role role;
}
