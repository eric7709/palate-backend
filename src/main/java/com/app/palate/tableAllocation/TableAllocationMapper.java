package com.app.palate.tableAllocation;

import com.app.palate.auth.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TableAllocationMapper {

    TableAllocationResponseDTO toDTO(TableAllocation allocation);

    @Mapping(target = "fullName", expression = "java(account.getFirstName() + \" \" + account.getLastName())")
    TableAllocationResponseDTO.StaffSummaryDTO toStaffSummaryDTO(Account account);
}