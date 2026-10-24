package com.travelgo.repository;

import com.travelgo.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for customer Tour Package Bookings.
 * 
 * - Enables saving, updating, deleting, and querying tour bookings in SQL Server.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Fetches all bookings made by a specific customer, sorted with newest bookings first.
     * 
     * [SPRING DATA JPA QUERY GENERATION]:
     * - "findByUser_Id": Filters by the customer's User foreign key ID.
     * - "OrderByCreatedAtDesc": Appends "ORDER BY created_at DESC" so latest bookings appear at the top.
     */
    List<Booking> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
