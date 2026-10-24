package com.travelgo.repository;

import com.travelgo.entity.TourPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for Tour Packages.
 * Contains both Spring Data JPA derived queries and custom JPQL queries with dynamic multi-criteria filtering.
 */
@Repository
public interface TourPackageRepository extends JpaRepository<TourPackage, Long> {

    /**
     * Searches tour packages by name containing a given keyword (case-insensitive).
     */
    List<TourPackage> findByNameContainingIgnoreCase(String name);

    /**
     * Retrieves all packages associated with a specific destination ID.
     */
    List<TourPackage> findByDestination_Id(Long destinationId);

    /**
     * Retrieves all packages categorized under a specific category ID.
     */
    List<TourPackage> findByCategory_Id(Long categoryId);

    /**
     * Returns only packages that are marked as ACTIVE (isActive = true) for public catalog browsing.
     */
    List<TourPackage> findByIsActiveTrue();

    /**
     * Advanced staff search filter across all packages (active & inactive).
     * 
     * [HOW IT WORKS IN JPQL]:
     * - Uses LEFT JOIN on destination and category.
     * - Safely handles NULL filters (:destinationId IS NULL OR d.id = :destinationId) so any combination
     *   of search terms, destination dropdown, and category dropdown works simultaneously!
     */
    @Query("SELECT p FROM TourPackage p LEFT JOIN p.destination d LEFT JOIN p.category c WHERE " +
           "(:destinationId IS NULL OR d.id = :destinationId) AND " +
           "(:categoryId IS NULL OR c.id = :categoryId) AND " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.country) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<TourPackage> filterPackages(
            @Param("query") String query,
            @Param("destinationId") Long destinationId,
            @Param("categoryId") Long categoryId);

    /**
     * Public search filter that only returns ACTIVE packages (p.isActive = true)
     * matching the customer's search keyword, destination filter, and category filter.
     */
    @Query("SELECT p FROM TourPackage p LEFT JOIN p.destination d LEFT JOIN p.category c WHERE " +
           "p.isActive = true AND " +
           "(:destinationId IS NULL OR d.id = :destinationId) AND " +
           "(:categoryId IS NULL OR c.id = :categoryId) AND " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.country) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<TourPackage> filterActivePackages(
            @Param("query") String query,
            @Param("destinationId") Long destinationId,
            @Param("categoryId") Long categoryId);
}

