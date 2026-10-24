package com.travelgo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Role entity - defines user roles in the system.
 * Each user is assigned exactly one role.
 * Roles are linked to permissions via the role_permissions join table.
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * A Role can have many Permissions, and a Permission can belong to multiple Roles.
     * 
     * [HOW IT WORKS IN SQL]:
     * - @JoinTable: Uses an intermediate join table called "role_permissions" with foreign keys
     *   "role_id" and "permission_id".
     * - fetch = FetchType.EAGER: Automatically loads permissions whenever the role is loaded so
     *   Spring Security can immediately verify access rights without extra database calls.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private java.util.Set<Permission> permissions = new java.util.HashSet<>();

    /**
     * Sets the creation timestamp when the role is first saved.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ---- Constructors ----

    /** Default constructor required by JPA */
    public Role() {}

    /** Convenience constructor for creating a new role */
    public Role(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    // ---- Getters and Setters ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.util.Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(java.util.Set<Permission> permissions) {
        this.permissions = permissions;
    }
}
