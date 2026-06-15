package com.app.palate.utils;

import org.springframework.stereotype.Component;

import com.app.palate.auth.Account;
import com.app.palate.auth.AccountRepository;
import com.app.palate.category.Category;
import com.app.palate.category.CategoryRepository;
import com.app.palate.customer.Customer;
import com.app.palate.customer.CustomerRepository;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.menuItem.MenuItem;
import com.app.palate.menuItem.MenuItemRepository;
import com.app.palate.restaurantTable.RestaurantTable;
import com.app.palate.restaurantTable.RestaurantTableRepository;
import com.app.palate.room.Room;
import com.app.palate.room.RoomRepository;
import com.app.palate.tableAllocation.TableAllocation;
import com.app.palate.tableAllocation.TableAllocationRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EntityResolver {

    private final TableAllocationRepository tableAllocationRepository;
    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final RoomRepository roomRepository;
    private final AccountRepository accountRepository;

    // =========================
    // Resolve Table Allocation
    // =========================
    public TableAllocation resolveTableAllocation(Long allocationId) {

        if (allocationId == null) {
            throw new BadRequestException("Allocation ID is required");
        }

        return tableAllocationRepository.findById(allocationId)
                .orElseThrow(() ->
                        new BadRequestException("Allocation not found"));
    }

    // =========================
    // Resolve Employee
    // =========================
    public Account resolveEmployee(Long accountId) {
        if (accountId == null) {
            throw new BadRequestException("Account ID is required");
        }
        return accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new BadRequestException("Account not found"));
    }

    // =========================
    // Resolve Customer
    // =========================
    public Customer resolveCustomer(Long customerId) {

        if (customerId == null) {
            throw new BadRequestException("Customer ID is required");
        }

        return customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new BadRequestException("Customer not found"));
    }

    // =========================
    // Resolve Menu Item
    // =========================
    public MenuItem resolveMenuItem(Long menuItemId) {

        if (menuItemId == null) {
            throw new BadRequestException("Menu Item ID is required");
        }

        return menuItemRepository.findById(menuItemId)
                .orElseThrow(() ->
                        new BadRequestException("Menu Item not found"));
    }

    // =========================
    // Resolve Category
    // =========================
    public Category resolveCategory(Long categoryId) {

        if (categoryId == null) {
            throw new BadRequestException("Category ID is required");
        }

        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new BadRequestException("Category not found"));
    }

    // =========================
    // Resolve Restaurant Table
    // =========================
    public RestaurantTable resolveRestaurantTable(Long tableId) {

        if (tableId == null) {
            throw new BadRequestException("Table ID is required");
        }

        return restaurantTableRepository.findById(tableId)
                .orElseThrow(() ->
                        new BadRequestException("Restaurant Table not found"));
    }

    // =========================
    // Resolve Room
    // =========================
    public Room resolveRoom(Long roomId) {

        if (roomId == null) {
            throw new BadRequestException("Room ID is required");
        }

        return roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new BadRequestException("Room not found"));
    }
}