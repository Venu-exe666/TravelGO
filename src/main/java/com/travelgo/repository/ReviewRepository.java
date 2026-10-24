package com.travelgo.repository;

import com.travelgo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for customer tour package reviews and ratings.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Fetches all reviews submitted for a specific tour package ID (displayed on the tour detail page).
     */
    List<Review> findByTourPackage_Id(Long packageId);

    /**
     * Fetches all reviews written by a specific user ID (displayed on the customer's personal profile/dashboard).
     */
    List<Review> findByUser_Id(Long userId);
}
