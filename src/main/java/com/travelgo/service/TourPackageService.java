package com.travelgo.service;

import com.travelgo.entity.TourPackage;
import com.travelgo.repository.TourPackageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TourPackageService {

    private final TourPackageRepository repository;

    /**
     * Injects TourPackageRepository.
     */
    public TourPackageService(TourPackageRepository repository) {
        this.repository = repository;
    }

    /** Retrieves all tour packages (for staff/admin inventory management) */
    public List<TourPackage> findAll() {
        return repository.findAll();
    }

    /** Finds a specific tour package by ID */
    public Optional<TourPackage> findById(Long id) {
        return repository.findById(id);
    }

    /** Creates or updates a tour package */
    public TourPackage save(TourPackage entity) {
        return repository.save(entity);
    }

    /** Deletes a package by ID */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /** Simple search by package name */
    public List<TourPackage> searchPackages(String query) {
        return repository.findByNameContainingIgnoreCase(query);
    }

    /** Finds all packages matching a specific destination */
    public List<TourPackage> findByDestinationId(Long destinationId) {
        return repository.findByDestination_Id(destinationId);
    }

    /** Finds all packages matching a specific category */
    public List<TourPackage> findByCategoryId(Long categoryId) {
        return repository.findByCategory_Id(categoryId);
    }

    /**
     * Filters all tour packages (active and inactive) for staff members.
     * Sanitizes the search query by trimming whitespace and converting empty strings to null.
     */
    public List<TourPackage> filterPackages(String query, Long destinationId, Long categoryId) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        return repository.filterPackages(cleanQuery, destinationId, categoryId);
    }

    /** Returns all packages marked as active for public catalog display */
    public List<TourPackage> findAllActive() {
        return repository.findByIsActiveTrue();
    }

    /**
     * Customer catalog multi-criteria search filter.
     * Only returns active packages matching the search term, destination dropdown, and category dropdown.
     */
    public List<TourPackage> filterActivePackages(String query, Long destinationId, Long categoryId) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        return repository.filterActivePackages(cleanQuery, destinationId, categoryId);
    }
}
