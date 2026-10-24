package com.travelgo.repository;

import com.travelgo.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access layer for customer contact inquiries and support messages.
 * 
 * - Inherits all standard Spring Data JPA CRUD operations for ContactMessage entities.
 */
@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
