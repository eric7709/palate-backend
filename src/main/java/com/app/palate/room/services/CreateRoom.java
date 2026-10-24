package com.app.palate.room.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.services.GetAccountById;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.room.Room;
import com.app.palate.room.RoomRepository;
import com.app.palate.room.dto.RoomRequestDTO;
import com.app.palate.service.QrCodeService;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Creates a room with its own QR code, and assigns the cashier if one is given. */
@Service
@RequiredArgsConstructor
public class CreateRoom {
    private final RoomRepository roomRepository;
    private final GetAccountById getAccountById;
    private final QrCodeService qrCodeService;

    @Transactional
    public Room create(RoomRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getRoomNumber(), "Room number");

        ensureRoomNumberNotTaken(request.getRoomNumber().trim());

        Room room = new Room();
        room.setRoomNumber(request.getRoomNumber().trim());
        room.setFloor(request.getFloor());
        room.setQrCode(qrCodeService.generateRandomQrCode());

        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }

        if (request.getCashierId() != null) {
            room.setCashier(getAccountById.execute(request.getCashierId()));
        }

        return roomRepository.save(room);
    }

    private void ensureRoomNumberNotTaken(String roomNumber) {
        if (roomRepository.existsByRoomNumber(roomNumber)) {
            throw new BadRequestException("Room number already exists");
        }
    }
}
