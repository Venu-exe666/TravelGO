package com.travelgo.repository;

import com.travelgo.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access layer for customer refund transactions.
 */
@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    /**
     * Finds the refund record associated with a specific payment transaction ID.
     */
    Optional<Refund> findByPayment_Id(Long paymentId);
}
