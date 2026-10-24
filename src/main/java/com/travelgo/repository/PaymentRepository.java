package com.travelgo.repository;

import com.travelgo.entity.Payment;
import com.travelgo.enums.PaymentStatus;
import com.travelgo.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for customer payments, invoices, and transaction histories.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds the first payment record matching a specific booking, payment type, and status.
     * Often used to check if a booking already has a PAID or PENDING invoice.
     */
    Optional<Payment> findFirstByBooking_IdAndPaymentTypeAndPaymentStatus(Long bookingId, PaymentType paymentType, PaymentStatus paymentStatus);

    /**
     * Finds all payments related to a particular booking.
     */
    List<Payment> findByBooking_Id(Long bookingId);

    /**
     * Finds all payments for a booking, sorted newest first.
     */
    List<Payment> findByBooking_IdOrderByCreatedAtDesc(Long bookingId);

    /**
     * Retrieves all system payments across all users, sorted by date descending for admin and staff reports.
     */
    List<Payment> findAllByOrderByCreatedAtDesc();

    /**
     * Filters payments by type (e.g., VISA_DOCUMENTATION vs FULL_PACKAGE), ordered newest first.
     */
    List<Payment> findByPaymentTypeOrderByCreatedAtDesc(PaymentType paymentType);

    /**
     * Filters payments by status (e.g., PENDING, PAID, REFUNDED), ordered newest first.
     */
    List<Payment> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus);
}
