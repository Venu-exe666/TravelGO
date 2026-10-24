package com.travelgo.repository;

import com.travelgo.entity.VisaApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for customer Visa Applications.
 */
@Repository
public interface VisaApplicationRepository extends JpaRepository<VisaApplication, Long> {

    /**
     * Finds the single visa application associated with a particular tour booking ID.
     */
    Optional<VisaApplication> findByBooking_Id(Long bookingId);

    /**
     * Finds all visa applications submitted by a particular user across all their bookings.
     */
    List<VisaApplication> findByBooking_User_Id(Long userId);
}
