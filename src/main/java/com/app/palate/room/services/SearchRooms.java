package com.app.palate.room.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.palate.auth.Account;
import com.app.palate.room.Room;
import com.app.palate.room.RoomRepository;
import com.app.palate.room.dto.RoomSearchCriteria;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

/**
 * Lists rooms one page at a time. The search text is matched against the room's own fields and
 * its cashier's details; status, floor and cashier narrow the results further.
 */
@Service
@RequiredArgsConstructor
public class SearchRooms {
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public Page<Room> search(RoomSearchCriteria criteria) {
        Sort sort = criteria.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(criteria.getSortBy()).descending()
                : Sort.by(criteria.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        return roomRepository.findAll(matching(criteria), pageable);
    }

    private Specification<Room> matching(RoomSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

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
    }
}
