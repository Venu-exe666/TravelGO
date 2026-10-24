package com.travelgo.repository;

import com.travelgo.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for travel Destinations (countries and cities).
 */
@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {

    /**
     * Searches for destinations matching either country or city name, ignoring uppercase/lowercase differences.
     * 
     * [SPRING DATA JPA MAGIC]:
     * - Generates: "SELECT * FROM destinations WHERE LOWER(country) LIKE LOWER(?) OR LOWER(city) LIKE LOWER(?)"
     */
    List<Destination> findByCountryContainingIgnoreCaseOrCityContainingIgnoreCase(String country, String city);
}
