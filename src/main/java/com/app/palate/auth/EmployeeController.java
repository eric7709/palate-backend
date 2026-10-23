package com.app.palate.auth;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/palate/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final AuthService authService;
    
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<AccountResponseDTO> getAllEmployees(EmployeeSearchCriteria criteria) {
        return authService.getAllEmployees(criteria)
                .map(AccountResponseDTO::mapToResponse);
    }
    

    // Create a new employee
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDTO createEmployee(@RequestBody AccountRequestDTO request) {
        Account account = authService.createEmployee(request);
        return AccountResponseDTO.mapToResponse(account);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AccountResponseDTO updateEmployee(
            @PathVariable Long id,
            @RequestBody AccountRequestDTO request) {
        Account account = authService.updateEmployee(id, request);
        return AccountResponseDTO.mapToResponse(account);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        authService.deleteEmployee(id);
    }
}