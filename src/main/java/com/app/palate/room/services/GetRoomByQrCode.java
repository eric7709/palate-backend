package com.app.palate.room.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.exceptions.ResourceNotFoundException;
import com.app.palate.room.Room;
import com.app.palate.room.RoomRepository;

import lombok.RequiredArgsConstructor;

/** Finds the room a guest scanned the QR code of. */
@Service
@RequiredArgsConstructor
public class GetRoomByQrCode {
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public Room get(String qrCode) {
        return roomRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found for QR code: " + qrCode));
    }
}
