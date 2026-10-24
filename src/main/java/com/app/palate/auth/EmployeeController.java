package com.app.palate.auth;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.app.palate.auth.dto.AccountRequestDTO;
import com.app.palate.auth.dto.AccountResponseDTO;
import com.app.palate.auth.dto.EmployeeSearchCriteria;
import com.app.palate.auth.services.CreateEmployee;
import com.app.palate.auth.services.DeleteEmployee;
import com.app.palate.auth.services.SearchEmployees;
import com.app.palate.auth.services.UpdateEmployee;
import com.app.palate.auth.utils.AccountMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/palate/employees")
public class EmployeeController {
    private final SearchEmployees searchEmployees;
    private final CreateEmployee createEmployee;
    private final UpdateEmployee updateEmployee;
    private final DeleteEmployee deleteEmployee;
    private final AccountMapper accountMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<AccountResponseDTO> search(EmployeeSearchCriteria criteria) {
        return searchEmployees.search(criteria).map(accountMapper::toResponse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDTO create(@RequestBody AccountRequestDTO request) {
        return accountMapper.toResponse(createEmployee.create(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AccountResponseDTO update(@PathVariable Long id, @RequestBody AccountRequestDTO request) {
        return accountMapper.toResponse(updateEmployee.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteEmployee.delete(id);
    }
}
