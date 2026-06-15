package com.app.palate.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findByEmail(String email);

    Optional<Account> findByPhoneNumber(String phoneNumber);
    

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    // Existing email/phone queries
    @Query("SELECT LOWER(a.email) FROM Account a WHERE LOWER(a.email) IN :emails")
    List<String> findExistingEmailsIgnoreCase(@Param("emails") List<String> emails);

    @Query("SELECT a.phoneNumber FROM Account a WHERE a.phoneNumber IN :phones")
    List<String> findExistingPhones(@Param("phones") List<String> phones);

    // ✅ NEW: get all accounts by role
    List<Account> findAllByRole(Role role);

    @Query("""
                SELECT new com.app.palate.auth.TopWaiterDTO(
                    o.waiter.id,
                    o.waiter.firstName,
                    o.waiter.lastName,
                    SUM(o.total)
                )
                FROM Order o
                WHERE o.createdAt BETWEEN :startDate AND :endDate
                GROUP BY o.waiter.id, o.waiter.firstName, o.waiter.lastName
                ORDER BY SUM(o.total) DESC
            """)
    List<TopWaiterDTO> findTopWaiters(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a.email FROM Account a WHERE LOWER(a.email) IN :emails")
    List<String> findExistingEmails(@Param("emails") List<String> emails);

    List<Account> findByRole(Role role);
}
