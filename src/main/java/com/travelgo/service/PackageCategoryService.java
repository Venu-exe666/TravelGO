package com.travelgo.service;

import com.travelgo.entity.PackageCategory;
import com.travelgo.repository.PackageCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PackageCategoryService {

    private final PackageCategoryRepository repository;

    /**
     * Injects PackageCategoryRepository to handle CRUD operations on categories.
     */
    public PackageCategoryService(PackageCategoryRepository repository) {
        this.repository = repository;
    }

    /** Retrieves all package categories */
    public List<PackageCategory> findAll() {
        return repository.findAll();
    }

    /** Finds a category by ID */
    public Optional<PackageCategory> findById(Long id) {
        return repository.findById(id);
    }

    /** Creates or updates a category */
    public PackageCategory save(PackageCategory entity) {
        return repository.save(entity);
    }

    /** Deletes a category by ID */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
