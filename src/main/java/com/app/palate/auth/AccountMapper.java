package com.app.palate.auth;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    MeDTO mapToDto(Account account);
}