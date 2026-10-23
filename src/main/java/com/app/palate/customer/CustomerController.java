package com.app.palate.customer;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.app.palate.customer.dto.CustomerRequestDTO;
import com.app.palate.customer.dto.CustomerSearchCriteria;
import com.app.palate.customer.services.CreateCustomer;
import com.app.palate.customer.services.DeleteCustomer;
import com.app.palate.customer.services.GetCustomerById;
import com.app.palate.customer.services.SearchCustomers;
import com.app.palate.customer.services.UpdateCustomer;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/palate/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CreateCustomer createCustomer;
    private final UpdateCustomer updateCustomer;
    private final SearchCustomers searchCustomers;
    private final GetCustomerById getCustomerById;
    private final DeleteCustomer deleteCustomer;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer create(@RequestBody CustomerRequestDTO request) {
        return createCustomer.create(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<Customer> search(CustomerSearchCriteria criteria) {
        return searchCustomers.search(criteria);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Customer get(@PathVariable Long id) {
        return getCustomerById.get(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Customer update(@PathVariable Long id, @RequestBody CustomerRequestDTO request) {
        return updateCustomer.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteCustomer.delete(id);
    }
}
