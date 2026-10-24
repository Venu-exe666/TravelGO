package com.travelgo.repository;

import com.travelgo.entity.Traveler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for individual Traveler details (passengers attached to a tour booking).
 */
@Repository
public interface TravelerRepository extends JpaRepository<Traveler, Long> {

    /**
     * Retrieves all travelers/passengers registered under a specific booking ID.
     */
    List<Traveler> findByBooking_Id(Long bookingId);
}
