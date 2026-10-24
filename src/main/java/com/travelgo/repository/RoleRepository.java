package com.travelgo.repository;

import com.travelgo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Data access layer for user roles (e.g., ADMIN, CUSTOMER, PACKAGE_MANAGER, VISA_OFFICER).
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its unique identifier name.
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * Checks if a role name already exists in the database.
     */
    boolean existsByRoleName(String roleName);
}
