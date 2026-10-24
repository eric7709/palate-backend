package com.app.palate.room.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.Account;
import com.app.palate.auth.AuthService;
import com.app.palate.room.Room;
import com.app.palate.room.RoomRepository;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/** Makes the given cashier responsible for a room, replacing any current one. */
@Service
@RequiredArgsConstructor
public class AllocateCashier {
    private final RoomRepository roomRepository;
    private final GetRoomById getRoomById;
    private final AuthService authService;

    @Transactional
    public Room allocate(Long roomId, Long cashierId) {
        ValidationUtils.requireNonNull(roomId, "Room ID");
        ValidationUtils.requireNonNull(cashierId, "Cashier ID");

        Room room = getRoomById.get(roomId);
        Account cashier = authService.getAccountById(cashierId);

        room.setCashier(cashier);
        return roomRepository.save(room);
    }
}
