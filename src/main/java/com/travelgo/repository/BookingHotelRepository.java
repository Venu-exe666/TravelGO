package com.travelgo.repository;

import com.travelgo.entity.BookingHotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access layer for BookingHotel entities (links a booking to a selected optional hotel).
 * 
 * - Extends JpaRepository to inherit standard CRUD operations (save, findById, findAll, deleteById)
 *   without having to write any SQL queries manually.
 */
@Repository
public interface BookingHotelRepository extends JpaRepository<BookingHotel, Long> {

    /**
     * Retrieves the optional hotel reservation associated with a specific booking ID.
     * 
     * [SPRING DATA JPA MAGIC]:
     * - The method name "findByBooking_Id" instructs Spring Data JPA to generate:
     *   "SELECT * FROM booking_hotels WHERE booking_id = ?"
     */
    Optional<BookingHotel> findByBooking_Id(Long bookingId);
}
