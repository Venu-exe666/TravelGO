package com.travelgo.service;

import com.travelgo.entity.Destination;
import com.travelgo.repository.DestinationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DestinationService {

    private final DestinationRepository repository;

    /**
     * Injects DestinationRepository to perform database operations on travel destinations.
     */
    public DestinationService(DestinationRepository repository) {
        this.repository = repository;
    }

    /** Retrieves all destinations from the database */
    public List<Destination> findAll() {
        return repository.findAll();
    }

    /** Finds a destination by ID */
    public Optional<Destination> findById(Long id) {
        return repository.findById(id);
    }

    /** Saves a new destination or updates an existing destination */
    public Destination save(Destination entity) {
        return repository.save(entity);
    }

    /** Deletes a destination by ID */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Searches destinations matching a keyword against city or country name.
     */
    public List<Destination> searchDestinations(String query) {
        return repository.findByCountryContainingIgnoreCaseOrCityContainingIgnoreCase(query, query);
    }
}
