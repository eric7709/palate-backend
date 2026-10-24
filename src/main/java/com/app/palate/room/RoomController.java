package com.app.palate.room;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.app.palate.room.dto.RoomRequestDTO;
import com.app.palate.room.dto.RoomResponseDTO;
import com.app.palate.room.dto.RoomSearchCriteria;
import com.app.palate.room.services.AllocateCashier;
import com.app.palate.room.services.CreateRoom;
import com.app.palate.room.services.CreateRoomsBulk;
import com.app.palate.room.services.DeallocateCashier;
import com.app.palate.room.services.DeleteRoom;
import com.app.palate.room.services.GetRoomById;
import com.app.palate.room.services.GetRoomByQrCode;
import com.app.palate.room.services.SearchRooms;
import com.app.palate.room.services.UpdateRoom;
import com.app.palate.room.utils.RoomMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/palate/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final CreateRoom createRoom;
    private final CreateRoomsBulk createRoomsBulk;
    private final UpdateRoom updateRoom;
    private final SearchRooms searchRooms;
    private final GetRoomById getRoomById;
    private final GetRoomByQrCode getRoomByQrCode;
    private final AllocateCashier allocateCashier;
    private final DeallocateCashier deallocateCashier;
    private final DeleteRoom deleteRoom;
    private final RoomMapper roomMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponseDTO create(@RequestBody RoomRequestDTO request) {
        return roomMapper.toDTO(createRoom.create(request));
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<RoomResponseDTO> createBulk(@RequestBody List<RoomRequestDTO> requests) {
        return createRoomsBulk.create(requests)
                .stream()
                .map(roomMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RoomResponseDTO get(@PathVariable Long id) {
        return roomMapper.toDTO(getRoomById.get(id));
    }

    @GetMapping("/qrcode/{token}")
    @ResponseStatus(HttpStatus.OK)
    public RoomResponseDTO getByQrCode(@PathVariable String token) {
        return roomMapper.toDTO(getRoomByQrCode.get(token));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<RoomResponseDTO> search(RoomSearchCriteria criteria) {
        return searchRooms.search(criteria).map(roomMapper::toDTO);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RoomResponseDTO update(@PathVariable Long id, @RequestBody RoomRequestDTO request) {
        return roomMapper.toDTO(updateRoom.update(id, request));
    }

    @PostMapping("/{id}/cashier/{cashierId}")
    @ResponseStatus(HttpStatus.OK)
    public RoomResponseDTO allocate(@PathVariable Long id, @PathVariable Long cashierId) {
        return roomMapper.toDTO(allocateCashier.allocate(id, cashierId));
    }

    @DeleteMapping("/{id}/cashier")
    @ResponseStatus(HttpStatus.OK)
    public RoomResponseDTO deallocate(@PathVariable Long id) {
        return roomMapper.toDTO(deallocateCashier.deallocate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteRoom.delete(id);
    }
}
