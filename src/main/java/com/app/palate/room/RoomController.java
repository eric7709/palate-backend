package com.app.palate.room;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/palate/rooms")
@RequiredArgsConstructor
public class RoomController {

        private final RoomService roomService;
        private final RoomMapper roomMapper;

        // =========================
        // Create Room
        // =========================
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public RoomResponseDTO createRoom(
                        @RequestBody RoomRequestDTO request) {

                return roomMapper.toDTO(
                                roomService.createRoom(request));
        }

        // =========================
        // Bulk Create Rooms
        // =========================
        @PostMapping("/bulk")
        @ResponseStatus(HttpStatus.CREATED)
        public List<RoomResponseDTO> createRoomsBulk(
                        @RequestBody List<RoomRequestDTO> requests) {

                return roomService.createRoomsBulk(requests)
                                .stream()
                                .map(roomMapper::toDTO)
                                .toList();
        }

        // =========================
        // Get Room By Id
        // =========================
        @GetMapping("/{id}")
        @ResponseStatus(HttpStatus.OK)
        public RoomResponseDTO getRoomById(
                        @PathVariable Long id) {

                return roomMapper.toDTO(
                                roomService.getRoomById(id));
        }

        @GetMapping("/qrcode/{token}")
        @ResponseStatus(HttpStatus.OK)
        public RoomResponseDTO getRoomByQrCode(@PathVariable String token) {
                return roomMapper.toDTO(roomService.getRoomByQrCode(token));
        }

        // =========================
        // Get All Rooms
        // =========================
        @GetMapping
        @ResponseStatus(HttpStatus.OK)
        public Page<RoomResponseDTO> getAllRooms(
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) RoomStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection) {
                // Pass the request parameters down to your updated service layer
                return roomService.getAllRooms(search, status, page, size, sortBy, sortDirection)
                                .map(roomMapper::toDTO);
        }

        // =========================
        // Update Room
        // =========================
        @PutMapping("/{id}")
        @ResponseStatus(HttpStatus.OK)
        public RoomResponseDTO updateRoom(
                        @PathVariable Long id,
                        @RequestBody RoomRequestDTO request) {

                return roomMapper.toDTO(
                                roomService.updateRoom(id, request));
        }

        @PostMapping("/{id}/cashier/{cashierId}")
        @ResponseStatus(HttpStatus.OK)
        public RoomResponseDTO allocateCashier(
                        @PathVariable Long id,
                        @PathVariable Long cashierId) {

                return roomMapper.toDTO(
                                roomService.allocateCashier(id, cashierId));
        }

        // =========================
        // Deallocate Cashier
        // =========================
        @DeleteMapping("/{id}/cashier")
        @ResponseStatus(HttpStatus.OK)
        public RoomResponseDTO deallocateCashier(
                        @PathVariable Long id) {

                return roomMapper.toDTO(
                                roomService.deallocateCashier(id));
        }

        // =========================
        // Delete Room
        // =========================
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void deleteRoom(
                        @PathVariable Long id) {

                roomService.deleteRoom(id);
        }
}