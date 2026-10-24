package com.travelgo.service;

import com.travelgo.entity.Permission;
import com.travelgo.entity.Role;
import com.travelgo.repository.PermissionRepository;
import com.travelgo.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service for role management operations.
 * Handles role CRUD and role-permission assignments.
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Injects RoleRepository and PermissionRepository.
     */
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /** Fetches all roles in the system */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /** Finds a role by ID */
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }

    /** Finds a role by its unique name (e.g. "ADMIN", "PACKAGE_MANAGER") */
    public Optional<Role> getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    /**
     * Updates the set of permissions assigned to a specific role.
     */
    @Transactional
    public void updateRolePermissions(Long roleId, Set<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        role.setPermissions(permissions);
        roleRepository.save(role);
    }

    /**
     * Returns the roles available when creating or editing staff members.
     * Excludes "CUSTOMER" because customers register themselves through the public sign-up page.
     */
    public List<Role> getStaffRoles() {
        return roleRepository.findAll().stream()
                .filter(r -> !"CUSTOMER".equals(r.getRoleName()))
                .toList();
    }
}
