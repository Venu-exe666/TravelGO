package com.travelgo.repository;

import com.travelgo.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
/**
 * Data access layer for global system settings (company info, business hours, maintenance toggle).
 * 
 * - Follows a Singleton row pattern: row with ID 1 stores the entire application's configuration.
 */
@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {
}
