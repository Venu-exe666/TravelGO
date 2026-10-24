package com.travelgo.service;

import com.travelgo.entity.Permission;
import com.travelgo.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Service for permission management operations.
 * Permissions are pre-seeded and managed by admin.
 */
@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    /**
     * Injects PermissionRepository to query system permissions.
     */
    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /** Fetches all permissions defined in the system */
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /** Finds a permission by ID */
    public Optional<Permission> getPermissionById(Long id) {
        return permissionRepository.findById(id);
    }

    /** Finds all permissions categorized under a specific module (e.g. USER, STAFF, BOOKING) */
    public List<Permission> getPermissionsByModule(String module) {
        return permissionRepository.findByModule(module);
    }

    /**
     * Retrieves unique module names (e.g. USER, BOOKING, VISA, SYSTEM)
     * used by admin views to render grouped permission checklists.
     */
    public List<String> getAllModules() {
        return permissionRepository.findDistinctModules();
    }
}
