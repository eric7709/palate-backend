package com.app.palate.dashboard;

import com.app.palate.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface DashboardCustomerRepository extends JpaRepository<Customer, Long> {

    @Query("""
        SELECT COUNT(c)
        FROM Customer c
        WHERE c.createdAt >= :from
          AND c.createdAt < :to
    """)
    long countCustomersBetween(@Param("from") Instant from, @Param("to") Instant to);
}