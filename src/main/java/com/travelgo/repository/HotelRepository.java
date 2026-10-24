package com.travelgo.repository;

import com.travelgo.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for Hotel add-on options available at various travel destinations.
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /**
     * Finds all hotels located in a specific destination ID.
     * 
     * [SPRING DATA JPA MAGIC]:
     * - Generates: "SELECT * FROM hotels WHERE destination_id = ?"
     */
    List<Hotel> findByDestination_Id(Long destinationId);
}
