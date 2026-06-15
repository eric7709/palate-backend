package com.app.palate.room;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <-- Add this import
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> { // <-- Extend here

    boolean existsByRoomNumber(String roomNumber);

    Room findByRoomNumber(String roomNumber);

    @Query("SELECT r.roomNumber FROM Room r WHERE r.roomNumber IN :roomNumbers")
    List<String> findExistingRoomNumbers(@Param("roomNumbers") List<String> roomNumbers);

    Optional<Room> findByQrCode(String qrCode);
}

