package com.app.palate.room.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.exceptions.BadRequestException;
import com.app.palate.room.Room;
import com.app.palate.room.RoomRepository;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetRoomById {
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public Room get(Long id) {
        ValidationUtils.requireNonNull(id, "Room ID");

        return roomRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Room not found"));
    }
}
