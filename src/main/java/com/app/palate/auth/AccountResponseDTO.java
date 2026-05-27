package com.app.palate.auth;

import java.util.ArrayList;
import java.util.List;

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

    public static List<AccountResponseDTO> mapAllToResponse(List<Account> accounts) {
        List<AccountResponseDTO> accountResponseDTOs = new ArrayList<>();
        for (Account account : accounts) {
            accountResponseDTOs.add(mapToResponse(account));
        }
        return accountResponseDTOs;
    }

    public static AccountResponseDTO mapToResponse(Account account) {
        AccountResponseDTO accountResponseDTO = new AccountResponseDTO();
        accountResponseDTO.setEmail(account.getEmail());
        accountResponseDTO.setFirstName(account.getFirstName());
        accountResponseDTO.setLastName(account.getLastName());
        accountResponseDTO.setId(account.getId());
        accountResponseDTO.setPhoneNumber(account.getPhoneNumber());
        accountResponseDTO.setRole(account.getRole());
        accountResponseDTO.setGender(account.getGender());
        return accountResponseDTO;
    }

}
