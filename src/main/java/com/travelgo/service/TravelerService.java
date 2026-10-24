package com.travelgo.service;

import com.travelgo.entity.Traveler;
import com.travelgo.repository.TravelerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TravelerService {

    private final TravelerRepository repository;

    /**
     * Injects TravelerRepository to manage passenger details for bookings.
     */
    public TravelerService(TravelerRepository repository) {
        this.repository = repository;
    }

    /** Retrieves all traveler records */
    public List<Traveler> findAll() {
        return repository.findAll();
    }

    /** Finds a specific traveler by ID */
    public Optional<Traveler> findById(Long id) {
        return repository.findById(id);
    }

    /** Saves or updates a traveler record */
    public Traveler save(Traveler entity) {
        return repository.save(entity);
    }

    /** Deletes a traveler record by ID */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Retrieves all passengers registered for a specific booking ID.
     */
    public List<Traveler> findByBookingId(Long bookingId) {
        return repository.findByBooking_Id(bookingId);
    }
}
