package com.app.palate.room.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.AuthService;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.room.Room;
import com.app.palate.room.RoomRepository;
import com.app.palate.room.dto.RoomRequestDTO;
import com.app.palate.service.QrCodeService;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Creates many rooms at once. If any one is invalid or already exists, none are saved. */
@Service
@RequiredArgsConstructor
public class CreateRoomsBulk {
    private final RoomRepository roomRepository;
    private final AuthService authService;
    private final QrCodeService qrCodeService;

    @Transactional
    public List<Room> create(List<RoomRequestDTO> requests) {
        ValidationUtils.requireNotEmpty(requests, "Bulk request list");

        List<Room> rooms = new ArrayList<>();
        Set<String> incomingNumbers = new HashSet<>();

        for (RoomRequestDTO req : requests) {
            ValidationUtils.requireNonNull(req, "Bulk item");
            ValidationUtils.requireNonBlank(req.getRoomNumber(), "Room number");

            String normalizedNumber = req.getRoomNumber().trim().toLowerCase();
            if (!incomingNumbers.add(normalizedNumber)) {
                throw new BadRequestException(
                        "Duplicate room number within payload list: " + req.getRoomNumber().trim());
            }
        }

        for (RoomRequestDTO request : requests) {
            ensureRoomNumberNotTaken(request.getRoomNumber().trim());

            Room room = new Room();
            room.setRoomNumber(request.getRoomNumber().trim());
            room.setFloor(request.getFloor());
            room.setQrCode(qrCodeService.generateRandomQrCode());

            if (request.getStatus() != null) {
                room.setStatus(request.getStatus());
            }

            if (request.getCashierId() != null) {
                room.setCashier(authService.getAccountById(request.getCashierId()));
            }

            rooms.add(room);
        }

        return roomRepository.saveAll(rooms);
    }

    private void ensureRoomNumberNotTaken(String roomNumber) {
        if (roomRepository.existsByRoomNumber(roomNumber)) {
            throw new BadRequestException("Room number already exists");
        }
    }
}
