package com.app.palate.restaurantTable;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.app.palate.restaurantTable.dto.RestaurantTableRequestDTO;
import com.app.palate.restaurantTable.dto.RestaurantTableResponseDTO;
import com.app.palate.restaurantTable.dto.RestaurantTableSearchCriteria;
import com.app.palate.restaurantTable.services.CreateRestaurantTable;
import com.app.palate.restaurantTable.services.CreateRestaurantTablesBulk;
import com.app.palate.restaurantTable.services.DeleteRestaurantTable;
import com.app.palate.restaurantTable.services.GetRestaurantTableById;
import com.app.palate.restaurantTable.services.GetRestaurantTableByQrCode;
import com.app.palate.restaurantTable.services.GetRestaurantTablesByAccount;
import com.app.palate.restaurantTable.services.SearchRestaurantTables;
import com.app.palate.restaurantTable.services.UpdateRestaurantTable;
import com.app.palate.restaurantTable.utils.RestaurantTableResponseMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/palate/tables")
@RequiredArgsConstructor
public class RestaurantTableController {
    private final CreateRestaurantTable createRestaurantTable;
    private final CreateRestaurantTablesBulk createRestaurantTablesBulk;
    private final UpdateRestaurantTable updateRestaurantTable;
    private final SearchRestaurantTables searchRestaurantTables;
    private final GetRestaurantTableById getRestaurantTableById;
    private final GetRestaurantTableByQrCode getRestaurantTableByQrCode;
    private final GetRestaurantTablesByAccount getRestaurantTablesByAccount;
    private final DeleteRestaurantTable deleteRestaurantTable;
    private final RestaurantTableResponseMapper tableResponseMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantTableResponseDTO create(@RequestBody RestaurantTableRequestDTO request) {
        return tableResponseMapper.mapToResponse(createRestaurantTable.create(request));
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<RestaurantTable> createBulk(@RequestBody List<RestaurantTableRequestDTO> requests) {
        return createRestaurantTablesBulk.create(requests);
    }

    @GetMapping("/qrcode/{token}")
    @ResponseStatus(HttpStatus.OK)
    public RestaurantTableResponseDTO getByQrCode(@PathVariable String token) {
        return tableResponseMapper.mapToResponse(getRestaurantTableByQrCode.get(token));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<RestaurantTableResponseDTO> search(RestaurantTableSearchCriteria criteria) {
        return searchRestaurantTables.search(criteria).map(tableResponseMapper::mapToResponse);
    }

    @GetMapping("/by-account")
    @ResponseStatus(HttpStatus.OK)
    public List<RestaurantTableResponseDTO> getByAccount(
            @RequestParam(required = false) Long waiterId,
            @RequestParam(required = false) Long cashierId) {
        return getRestaurantTablesByAccount.get(waiterId, cashierId)
                .stream()
                .map(tableResponseMapper::mapToResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RestaurantTableResponseDTO get(@PathVariable Long id) {
        return tableResponseMapper.mapToResponse(getRestaurantTableById.get(id));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RestaurantTableResponseDTO update(@PathVariable Long id,
            @RequestBody RestaurantTableRequestDTO request) {
        return tableResponseMapper.mapToResponse(updateRestaurantTable.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteRestaurantTable.delete(id);
    }
}
