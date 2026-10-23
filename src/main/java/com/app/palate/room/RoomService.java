package com.app.palate.room;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.app.palate.auth.Account;
import com.app.palate.auth.AuthService;
import com.app.palate.exceptions.BadRequestException;
import com.app.palate.exceptions.ResourceNotFoundException;
import com.app.palate.service.QrCodeService;
import com.app.palate.utils.ValidationUtils;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final AuthService authService;
    private final QrCodeService qrCodeService;

    @Transactional
    public Room createRoom(RoomRequestDTO request) {
        ValidationUtils.requireNonNull(request, "Request body");
        ValidationUtils.requireNonBlank(request.getRoomNumber(), "Room number");

        validateUniqueRoomNumberOnCreation(request.getRoomNumber().trim());

        Room room = new Room();
        room.setRoomNumber(request.getRoomNumber().trim());
        room.setFloor(request.getFloor());

        String generatedQr = qrCodeService.generateRandomQrCode();
        room.setQrCode(generatedQr);

        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }

        if (request.getCashierId() != null) {
            room.setCashier(authService.getAccountById(request.getCashierId()));
        }

        return roomRepository.save(room);
    }

    // =========================
    // Get & Search Rooms
    // =========================
    public Room getRoomById(Long id) {
        ValidationUtils.requireNonNull(id, "Room ID");
        return roomRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Room not found"));
    }

    public Page<Room> getAllRooms(RoomSearchCriteria criteria) {

        Sort sort = criteria.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(criteria.getSortBy()).descending()
                : Sort.by(criteria.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        Specification<Room> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Text search across room metrics and assigned staff names
            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getSearch().trim().toLowerCase() + "%";

                Join<Room, Account> cashierJoin = root.join("cashier", JoinType.LEFT);

                Predicate byRoomNumber = cb.like(cb.lower(root.get("roomNumber")), pattern);
                Predicate byFloor = cb.like(cb.toString(root.get("floor")), pattern);
                Predicate byQrCode = cb.like(cb.lower(root.get("qrCode")), pattern);
                Predicate byStatus = cb.like(cb.lower(root.get("status").as(String.class)), pattern);
                Predicate byId = cb.like(root.get("id").as(String.class), pattern);
                Predicate byCreatedAt = cb.like(root.get("createdAt").as(String.class), pattern);
                Predicate byUpdatedAt = cb.like(root.get("updatedAt").as(String.class), pattern);
                Predicate byCashierId = cb.like(cashierJoin.get("id").as(String.class), pattern);

                Predicate byCashierFirst = cb.like(cb.lower(cashierJoin.get("firstName")), pattern);
                Predicate byCashierLast = cb.like(cb.lower(cashierJoin.get("lastName")), pattern);
                Predicate byCashierEmail = cb.like(cb.lower(cashierJoin.get("email")), pattern);
                Predicate byCashierPhone = cb.like(cashierJoin.get("phoneNumber"), pattern);

                predicates.add(cb.or(byRoomNumber, byFloor, byQrCode, byStatus, byId,
                        byCreatedAt, byUpdatedAt, byCashierId,
                        byCashierFirst, byCashierLast, byCashierEmail, byCashierPhone));
                query.distinct(true);
            }

            // Status enum filtering
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getFloor() != null) {
                predicates.add(cb.equal(root.get("floor"), criteria.getFloor()));
            }
            if (criteria.getCashierId() != null) {
                predicates.add(cb.equal(root.get("cashier").get("id"), criteria.getCashierId()));
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };

        return roomRepository.findAll(spec, pageable);
    }

    public Room getRoomByQrCode(String qrCode) {
        return roomRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found for QR code: " + qrCode));
    }

    // =========================
    // Update Room
    // =========================
    @Transactional
    public Room updateRoom(Long id, RoomRequestDTO request) {
        ValidationUtils.requireNonNull(id, "Room ID");
        ValidationUtils.requireNonNull(request, "Request body");

        Room room = getRoomById(id);

        ValidationUtils.requireNonBlank(request.getRoomNumber(), "Room number");
        validateUniqueRoomNumberOnUpdate(request.getRoomNumber().trim(), id);

        room.setRoomNumber(request.getRoomNumber().trim());
        room.setFloor(request.getFloor());

        room.setQrCode(qrCodeService.generateRandomQrCode());

        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }

        if (request.getCashierId() != null) {
            room.setCashier(authService.getAccountById(request.getCashierId()));
        } else {
            room.setCashier(null);
        }

        return roomRepository.save(room);
    }

    // =========================
    // Delete Room
    // =========================
    @Transactional
    public void deleteRoom(Long id) {
        ValidationUtils.requireNonNull(id, "Room ID");
        getRoomById(id);
        roomRepository.deleteById(id);
    }

    // =========================
    // Bulk Create Rooms
    // =========================
    @Transactional
    public List<Room> createRoomsBulk(List<RoomRequestDTO> requests) {
        ValidationUtils.requireNotEmpty(requests, "Bulk request list");

        List<Room> rooms = new ArrayList<>();
        java.util.Set<String> incomingNumbers = new java.util.HashSet<>();

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
            validateUniqueRoomNumberOnCreation(request.getRoomNumber().trim());

            Room room = new Room();
            room.setRoomNumber(request.getRoomNumber().trim());
            room.setFloor(request.getFloor());

            // Auto-assign code strings cleanly for bulk creations
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

    // =========================
    // Cashier Allocation
    // =========================
    @Transactional
    public Room allocateCashier(Long roomId, Long cashierId) {
        ValidationUtils.requireNonNull(roomId, "Room ID");
        ValidationUtils.requireNonNull(cashierId, "Cashier ID");

        Room room = getRoomById(roomId);
        Account cashier = authService.getAccountById(cashierId);

        room.setCashier(cashier);
        return roomRepository.save(room);
    }

    @Transactional
    public Room deallocateCashier(Long roomId) {
        ValidationUtils.requireNonNull(roomId, "Room ID");

        Room room = getRoomById(roomId);
        room.setCashier(null);

        return roomRepository.save(room);
    }

    // =========================
    // Validation
    // =========================
    private void validateUniqueRoomNumberOnCreation(String roomNumber) {
        if (roomRepository.existsByRoomNumber(roomNumber)) {
            throw new BadRequestException("Room number already exists");
        }
    }

    private void validateUniqueRoomNumberOnUpdate(String roomNumber, Long id) {
        Room room = roomRepository.findByRoomNumber(roomNumber);
        if (room != null && !room.getId().equals(id)) {
            throw new BadRequestException("Room number already exists");
        }
    }
}