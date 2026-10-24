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

/** Replaces a room's number, floor and cashier (leaving the cashier out removes them) and issues a new QR code. */
@Service
@RequiredArgsConstructor
public class UpdateRoom {
    private final RoomRepository roomRepository;
    private final GetRoomById getRoomById;
    private final GetAccountById getAccountById;
    private final QrCodeService qrCodeService;

    @Transactional
    public Room update(Long id, RoomRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Room ID");
        ValidationUtils.requireNonNull(request, "Request body");

        Room room = getRoomById.get(id);

        ValidationUtils.requireNonBlank(request.getRoomNumber(), "Room number");
        ensureRoomNumberNotTakenByAnother(request.getRoomNumber().trim(), id);

        room.setRoomNumber(request.getRoomNumber().trim());
        room.setFloor(request.getFloor());
        room.setQrCode(qrCodeService.generateRandomQrCode());

        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }

        if (request.getCashierId() != null) {
            room.setCashier(getAccountById.execute(request.getCashierId()));
        } else {
            room.setCashier(null);
        }

        return roomRepository.save(room);
    }

    private void ensureRoomNumberNotTakenByAnother(String roomNumber, Long id) {
        Room room = roomRepository.findByRoomNumber(roomNumber);
        if (room != null && !room.getId().equals(id)) {
            throw new BadRequestException("Room number already exists");
        }
    }
}
