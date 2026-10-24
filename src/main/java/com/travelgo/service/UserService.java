package com.travelgo.service;

import com.travelgo.entity.Role;
import com.travelgo.entity.User;
import com.travelgo.repository.RoleRepository;
import com.travelgo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service for user management operations.
 * Handles registration, CRUD, and account status management for all user types.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Injects UserRepository, RoleRepository, and BCrypt PasswordEncoder beans.
     */
    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---- Registration ----

    /**
     * Registers a new customer account when they submit the sign-up form.
     */
    @Transactional
    public User registerCustomer(String name, String email, String password, String phone, String address) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered: " + email);
        }

        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("CUSTOMER role not found"));

        User user = new User(name, email, passwordEncoder.encode(password), customerRole);
        user.setPhone(phone);
        user.setAddress(address);
        return userRepository.save(user);
    }

    // ---- Staff Creation (by Admin) ----

    /**
     * Allows an administrator to provision a new staff member (e.g., VISA_OFFICER, PACKAGE_MANAGER).
     */
    @Transactional
    public User createStaffAccount(String name, String email, String password,
                                   String phone, String address, String roleName) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered: " + email);
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        User user = new User(name, email, passwordEncoder.encode(password), role);
        user.setPhone(phone);
        user.setAddress(address);
        return userRepository.save(user);
    }

    // ---- Find Operations ----

    /** Fetches all registered users across all roles */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** Finds a user by their primary key ID */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /** Finds a user by email (used by session resolvers in controllers) */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /** Searches users by matching either name or email substring */
    public List<User> searchUsers(String query) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    /** Returns all users with role 'CUSTOMER' */
    public List<User> getCustomers() {
        return userRepository.findByRoleRoleName("CUSTOMER");
    }

    /** Returns currently active staff members (all non-customer roles with active = true) */
    public List<User> getStaffMembers() {
        // Staff = everyone except CUSTOMER
        return userRepository.findByRoleRoleNameNotAndIsActive("CUSTOMER", true);
    }

    /** Returns all staff members regardless of whether they are active or deactivated */
    public List<User> getAllStaff() {
        // All non-customer users regardless of active status
        List<User> all = userRepository.findAll();
        return all.stream()
                .filter(u -> !"CUSTOMER".equals(u.getRole().getRoleName()))
                .toList();
    }

    // ---- Update Operations ----

    /** Updates customer profile details (name, phone, address) */
    @Transactional
    public User updateUser(Long id, String name, String phone, String address) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        return userRepository.save(user);
    }

    /**
     * Updates the profile owned by the currently authenticated customer.
     * The lookup is intentionally performed with the authenticated email so a
     * customer cannot submit another user's database ID and edit that account.
     */
    @Transactional
    public User updateCustomerProfile(String authenticatedEmail, String name, String phone, String address) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RuntimeException("Signed-in customer account was not found."));

        if (user.getRole() == null || !"CUSTOMER".equals(user.getRole().getRoleName())) {
            throw new IllegalStateException("Only customer accounts can use customer profile settings.");
        }

        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        return userRepository.save(user);
    }

    /** Updates a staff member's details and optionally reassigns their role */
    @Transactional
    public User updateStaff(Long id, String name, String phone, String address, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);

        if (roleName != null && !roleName.isEmpty()) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            user.setRole(role);
        }

        return userRepository.save(user);
    }

    // ---- Status Management ----

    /**
     * Toggles an account between Active and Deactivated (banned/suspended).
     * Deactivated users cannot log into the system because user.isActive() returns false to Spring Security.
     */
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    // ---- Role Assignment ----

    /** Reassigns a user's security role (e.g. promoting a customer or changing staff departments) */
    @Transactional
    public void assignRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        user.setRole(role);
        userRepository.save(user);
    }

    // ---- Statistics ----

    /** Total count of all accounts in the database */
    public long getTotalUsers() {
        return userRepository.count();
    }

    /** Count of active user accounts */
    public long getActiveUsers() {
        return userRepository.countByIsActive(true);
    }

    /** Count of deactivated accounts */
    public long getInactiveUsers() {
        return userRepository.countByIsActive(false);
    }

    /** Count of total staff accounts */
    public long getTotalStaff() {
        return userRepository.countByRoleRoleNameNot("CUSTOMER");
    }
}
