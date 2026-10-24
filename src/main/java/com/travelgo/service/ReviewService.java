package com.travelgo.service;

import com.travelgo.entity.Review;
import com.travelgo.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository repository;

    public ReviewService(ReviewRepository repository) {
        this.repository = repository;
    }

    public List<Review> findAll() {
        return repository.findAll();
    }

    public Optional<Review> findById(Long id) {
        return repository.findById(id);
    }

    public Review save(Review entity) {
        return repository.save(entity);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<Review> findByPackageId(Long packageId) {
        return repository.findByTourPackage_Id(packageId);
    }

    public List<Review> findByUserId(Long userId) {
        return repository.findByUser_Id(userId);
    }
}
