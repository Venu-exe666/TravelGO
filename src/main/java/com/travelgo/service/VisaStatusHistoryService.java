package com.travelgo.service;

import com.travelgo.entity.VisaStatusHistory;
import com.travelgo.repository.VisaStatusHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VisaStatusHistoryService {

    private final VisaStatusHistoryRepository repository;

    /**
     * Injects VisaStatusHistoryRepository to record and query visa status audit trails.
     */
    public VisaStatusHistoryService(VisaStatusHistoryRepository repository) {
        this.repository = repository;
    }

    /** Retrieves all visa status transition history records */
    public List<VisaStatusHistory> findAll() {
        return repository.findAll();
    }

    /** Finds a specific history entry by ID */
    public Optional<VisaStatusHistory> findById(Long id) {
        return repository.findById(id);
    }

    /** Saves a new status transition audit log entry */
    public VisaStatusHistory save(VisaStatusHistory entity) {
        return repository.save(entity);
    }

    /** Deletes an audit log entry by ID */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
