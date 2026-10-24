package com.app.palate.auth.utils;

import java.util.List;

import org.mapstruct.Mapper;

import com.app.palate.auth.Account;
import com.app.palate.auth.dto.AccountResponseDTO;
import com.app.palate.auth.dto.MeDTO;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    MeDTO toMe(Account account);

    AccountResponseDTO toResponse(Account account);

    List<AccountResponseDTO> mapAllToResponse(List<Account> accounts);
}
