package com.app.palate.restaurantTable;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/palate/tables")
@RequiredArgsConstructor
public class RestaurantTableController {

    private final RestaurantTableService restaurantTableService;
    // 1. Inject the MapStruct mapper instance
    private final RestaurantTableResponseMapper tableResponseMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantTableResponseDTO createTable(@RequestBody RestaurantTableRequestDTO request) {
        RestaurantTable table = restaurantTableService.createTable(request);
        return tableResponseMapper.mapToResponse(table);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<RestaurantTable> createTablesBulk(@RequestBody List<RestaurantTableRequestDTO> requests) {
        return restaurantTableService.createTablesBulk(requests);
    }

    @GetMapping("/qrcode/{token}")
    @ResponseStatus(HttpStatus.OK)
    public RestaurantTableResponseDTO getTableByQrCode(@PathVariable String token) {
        RestaurantTable table = restaurantTableService.getTableByQrCode(token);
        return tableResponseMapper.mapToResponse(table);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<RestaurantTableResponseDTO> getAllTables(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return restaurantTableService.getAllTables(search, status, page, size, sortBy, sortDirection)
                .map(tableResponseMapper::mapToResponse);
    }

    @GetMapping("/by-account")
    @ResponseStatus(HttpStatus.OK)
    public List<RestaurantTableResponseDTO> getTablesByAccount(
            @RequestParam(required = false) Long waiterId,
            @RequestParam(required = false) Long cashierId) {
        return restaurantTableService.getTablesByAccount(waiterId, cashierId)
                .stream()
                .map(tableResponseMapper::mapToResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RestaurantTableResponseDTO getTableById(@PathVariable Long id) {
        RestaurantTable table = restaurantTableService.getTableById(id);
        return tableResponseMapper.mapToResponse(table);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RestaurantTableResponseDTO updateTable(@PathVariable Long id,
            @RequestBody RestaurantTableRequestDTO request) {
        RestaurantTable table = restaurantTableService.updateTable(id, request);
        return tableResponseMapper.mapToResponse(table);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTable(@PathVariable Long id) {
        restaurantTableService.deleteTable(id);
    }
}