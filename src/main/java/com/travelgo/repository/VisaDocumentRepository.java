package com.travelgo.repository;

import com.travelgo.entity.VisaDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for customer uploaded visa files (passports, photos, bank statements).
 */
@Repository
public interface VisaDocumentRepository extends JpaRepository<VisaDocument, Long> {

    /**
     * Retrieves all uploaded document records belonging to a specific visa application ID.
     */
    List<VisaDocument> findByVisaApplication_Id(Long visaApplicationId);
}
