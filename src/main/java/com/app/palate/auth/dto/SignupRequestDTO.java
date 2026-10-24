package com.app.palate.auth.dto;

import com.app.palate.auth.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDTO {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Role role;
    
}
