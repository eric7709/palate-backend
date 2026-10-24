package com.app.palate.room.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.room.Room;
import com.app.palate.room.RoomRepository;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Removes the cashier from a room. */
@Service
@RequiredArgsConstructor
public class DeallocateCashier {
    private final RoomRepository roomRepository;
    private final GetRoomById getRoomById;

    @Transactional
    public Room deallocate(Long roomId) {
        ValidationUtils.requireNonNull(roomId, "Room ID");

        Room room = getRoomById.get(roomId);
        room.setCashier(null);

        return roomRepository.save(room);
    }
}
