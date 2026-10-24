package com.travelgo.repository;

import com.travelgo.entity.VisaStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access layer for Visa Status Audit Trail (tracks who changed a visa status, when, and remarks).
 * 
 * - Inherits CRUD operations from JpaRepository to log and fetch status history timeline events.
 */
@Repository
public interface VisaStatusHistoryRepository extends JpaRepository<VisaStatusHistory, Long> {
}
