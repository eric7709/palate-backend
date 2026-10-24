package com.app.palate.room.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.room.RoomRepository;
import com.app.palate.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteRoom {
    private final RoomRepository roomRepository;
    private final GetRoomById getRoomById;

    @Transactional
    public void delete(Long id) {
        ValidationUtils.requireNonNull(id, "Room ID");
        getRoomById.get(id);
        roomRepository.deleteById(id);
    }
}
