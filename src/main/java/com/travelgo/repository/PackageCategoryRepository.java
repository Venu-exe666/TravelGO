package com.travelgo.repository;

import com.travelgo.entity.PackageCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access layer for Tour Package Categories (e.g. Cultural Heritage, Luxury & Leisure, Adventure & Safari).
 * 
 * - Inherits CRUD operations from JpaRepository to create, read, update, and delete categories.
 */
@Repository
public interface PackageCategoryRepository extends JpaRepository<PackageCategory, Long> {
}
