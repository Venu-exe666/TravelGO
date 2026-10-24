package com.travelgo.service;

import com.travelgo.entity.Hotel;
import com.travelgo.repository.HotelRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HotelService {

    private final HotelRepository repository;

    /**
     * Injects HotelRepository to perform hotel operations.
     */
    public HotelService(HotelRepository repository) {
        this.repository = repository;
    }

    /** Retrieves all hotels */
    public List<Hotel> findAll() {
        return repository.findAll();
    }

    /** Finds a hotel by ID */
    public Optional<Hotel> findById(Long id) {
        return repository.findById(id);
    }

    /** Saves or updates a hotel */
    public Hotel save(Hotel entity) {
        return repository.save(entity);
    }

    /** Deletes a hotel by ID */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Finds hotels located in a specific destination ID (used to populate hotel add-ons during booking).
     */
    public List<Hotel> findByDestinationId(Long destinationId) {
        return repository.findByDestination_Id(destinationId);
    }
}
