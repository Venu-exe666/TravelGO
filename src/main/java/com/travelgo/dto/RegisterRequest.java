package com.travelgo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) for the customer registration form.
 * 
 * [HOW IT WORKS]:
 * - Collects and validates raw user input from the sign-up HTML form before creating a User entity.
 * - Jakarta Validation annotations (@NotBlank, @Email, @Size, @Pattern) enforce security rules automatically.
 */
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @jakarta.validation.constraints.Pattern(regexp = "^$|^\\+?[0-9\\-\\s()]{7,20}$", message = "Please enter a valid phone number (e.g. +1234567890 or 0771234567)")
    private String phone;

    private String address;

    // ---- Getters and Setters (Standard Java bean encapsulation) ----

    /** Gets the customer's full name */
    public String getName() { return name; }
    /** Sets the customer's full name */
    public void setName(String name) { this.name = name; }

    /** Gets the customer's email address */
    public String getEmail() { return email; }
    /** Sets the customer's email address */
    public void setEmail(String email) { this.email = email; }

    /** Gets the plain text password input */
    public String getPassword() { return password; }
    /** Sets the plain text password input */
    public void setPassword(String password) { this.password = password; }

    /** Gets the password confirmation input to ensure passwords match */
    public String getConfirmPassword() { return confirmPassword; }
    /** Sets the password confirmation input */
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    /** Gets the customer's contact phone number */
    public String getPhone() { return phone; }
    /** Sets the customer's contact phone number */
    public void setPhone(String phone) { this.phone = phone; }

    /** Gets the customer's physical home or billing address */
    public String getAddress() { return address; }
    /** Sets the customer's physical home or billing address */
    public void setAddress(String address) { this.address = address; }
}
