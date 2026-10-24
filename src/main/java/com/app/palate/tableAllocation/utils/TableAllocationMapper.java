package com.app.palate.tableAllocation.utils;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.app.palate.auth.Account;
import com.app.palate.tableAllocation.TableAllocation;
import com.app.palate.tableAllocation.dto.TableAllocationResponseDTO;

@Mapper(componentModel = "spring")
public interface TableAllocationMapper {

    TableAllocationResponseDTO toDTO(TableAllocation allocation);

    @Mapping(target = "fullName", expression = "java(account.getFirstName() + \" \" + account.getLastName())")
    TableAllocationResponseDTO.StaffSummaryDTO toStaffSummaryDTO(Account account);
}
