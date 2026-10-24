package com.travelgo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) used by administrators to provision new staff accounts.
 * 
 * [HOW IT WORKS]:
 * - Captures input from the admin "Add Staff Member" form.
 * - Enforces validations on name, work email, temporary password, phone, and assigned role.
 */
public class StaffCreateRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @jakarta.validation.constraints.Pattern(regexp = "^$|^\\+?[0-9\\-\\s()]{7,20}$", message = "Please enter a valid phone number (e.g. +1234567890)")
    private String phone;

    private String address;

    @NotBlank(message = "Role is required")
    private String roleName;

    // ---- Getters and Setters (Encapsulation) ----

    /** Gets the staff member's full name */
    public String getName() { return name; }
    /** Sets the staff member's full name */
    public void setName(String name) { this.name = name; }

    /** Gets the staff member's official email address */
    public String getEmail() { return email; }
    /** Sets the staff member's official email address */
    public void setEmail(String email) { this.email = email; }

    /** Gets the initial password assigned by the admin */
    public String getPassword() { return password; }
    /** Sets the initial password assigned by the admin */
    public void setPassword(String password) { this.password = password; }

    /** Gets the staff contact phone number */
    public String getPhone() { return phone; }
    /** Sets the staff contact phone number */
    public void setPhone(String phone) { this.phone = phone; }

    /** Gets the staff residential/office address */
    public String getAddress() { return address; }
    /** Sets the staff residential/office address */
    public void setAddress(String address) { this.address = address; }

    /** Gets the designated role name (e.g., PACKAGE_MANAGER, VISA_OFFICER) */
    public String getRoleName() { return roleName; }
    /** Sets the designated role name */
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
