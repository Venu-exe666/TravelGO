package com.travelgo.repository;

import com.travelgo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
/**
 * Data access layer for User accounts (Customers, Staff, Administrators).
 * Core interface used for authentication, registration, search, and admin dashboards.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Used by Spring Security during login to look up an account by its unique email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * Used during registration to check whether an email is already taken.
     */
    boolean existsByEmail(String email);

    /**
     * Finds all users with a specific role name (e.g., "CUSTOMER", "ADMIN", "VISA_OFFICER").
     */
    List<User> findByRoleRoleName(String roleName);

    /**
     * Searches users by either their name OR email (case-insensitive substring match).
     */
    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);

    /**
     * Counts how many active (or deactivated) accounts exist in the system for dashboard metric cards.
     */
    long countByIsActive(boolean isActive);

    /**
     * Counts all staff members by checking accounts whose role is NOT "CUSTOMER".
     */
    long countByRoleRoleNameNot(String roleName);

    /**
     * Retrieves all staff members (role != "CUSTOMER") who are currently active.
     */
    List<User> findByRoleRoleNameNotAndIsActive(String roleName, boolean isActive);
}
