package com.travelgo.repository;

import com.travelgo.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Data access layer for granular RBAC (Role-Based Access Control) permissions.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Looks up a permission by its unique name (e.g. "USER_VIEW", "BOOKING_MANAGE").
     */
    Optional<Permission> findByPermissionName(String permissionName);

    /**
     * Checks if a permission with the given name already exists in the database.
     */
    boolean existsByPermissionName(String permissionName);

    /**
     * Groups and finds permissions belonging to a specific functional module (e.g. "USER", "SYSTEM", "PACKAGE").
     */
    List<Permission> findByModule(String module);

    /**
     * Custom JPQL query that returns a list of unique module names in alphabetical order.
     * Used in the Admin UI to group permissions neatly by categories.
     */
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p.module FROM Permission p ORDER BY p.module")
    List<String> findDistinctModules();
}
