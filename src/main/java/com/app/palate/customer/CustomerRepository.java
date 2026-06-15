package com.app.palate.customer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {   

    Customer findByEmail(String email);

    Optional<Customer> findByPhoneNumber(String phoneNumber);


    @Query("""
                SELECT new com.app.palate.customer.TopCustomerDTO(
                    o.customer.id,
                    o.customer.name,
                    SUM(o.total)
                )
                FROM Order o
                WHERE o.createdAt BETWEEN :startDate AND :endDate
                GROUP BY o.customer.id, o.customer.name
                ORDER BY SUM(o.total) DESC
            """)
    List<TopCustomerDTO> findTopCustomers(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("""
                SELECT LOWER(c.email)
                FROM Customer c
                WHERE LOWER(c.email) IN :emails
            """)
    List<String> findExistingEmailsIgnoreCase(@Param("emails") List<String> emails);

    @Query("""
                SELECT c.phoneNumber
                FROM Customer c
                WHERE c.phoneNumber IN :phones
            """)
    List<String> findExistingPhones(@Param("phones") List<String> phones);
}
