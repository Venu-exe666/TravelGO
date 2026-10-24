package com.travelgo.service;

import com.travelgo.entity.VisaDocument;
import com.travelgo.repository.VisaDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VisaDocumentService {

    private final VisaDocumentRepository repository;

    /**
     * Injects VisaDocumentRepository.
     */
    public VisaDocumentService(VisaDocumentRepository repository) {
        this.repository = repository;
    }

    /** Retrieves all uploaded visa documents */
    public List<VisaDocument> findAll() {
        return repository.findAll();
    }

    /** Finds a document by ID */
    public Optional<VisaDocument> findById(Long id) {
        return repository.findById(id);
    }

    /** Saves a newly uploaded document record with its storage path URL */
    public VisaDocument save(VisaDocument entity) {
        return repository.save(entity);
    }

    /** Deletes a document record by ID */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /** Retrieves all files uploaded for a specific visa application */
    public List<VisaDocument> findByVisaApplicationId(Long visaApplicationId) {
        return repository.findByVisaApplication_Id(visaApplicationId);
    }

    /**
     * Marks an individual uploaded file (e.g. passport copy) as officially verified by the Visa Officer.
     */
    public VisaDocument verifyDocument(Long docId) {
        VisaDocument doc = repository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Visa document not found"));
        doc.setVerified(true);
        return repository.save(doc);
    }
}
