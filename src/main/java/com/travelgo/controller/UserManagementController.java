package com.travelgo.controller;

import com.travelgo.entity.User;
import com.travelgo.service.RoleService;
import com.travelgo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for User Management in Admin panel.
 */
@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    private final UserService userService;
    private final RoleService roleService;

    public UserManagementController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String listUsers(@RequestParam(value = "search", required = false) String search, Model model) {
        List<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search.trim());
        } else {
            users = userService.getAllUsers();
        }
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        return "admin/users";
    }

    @GetMapping("/{id}")
    public String viewUser(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return userService.getUserById(id).map(user -> {
            model.addAttribute("user", user);
            model.addAttribute("roles", roleService.getAllRoles());
            return "admin/user-detail";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found with ID: " + id);
            return "redirect:/admin/users";
        });
    }

    @PostMapping("/{id}/update")
    public String updateUser(@PathVariable("id") Long id,
                             @RequestParam("name") String name,
                             @RequestParam("phone") String phone,
                             @RequestParam("address") String address,
                             RedirectAttributes redirectAttributes) {
        if (name == null || name.trim().length() < 2 || name.trim().length() > 100) {
            redirectAttributes.addFlashAttribute("errorMessage", "Name must be between 2 and 100 characters.");
            return "redirect:/admin/users/" + id;
        }

        if (phone != null && !phone.trim().isEmpty() && !phone.trim().matches("^\\+?[0-9\\-\\s()]{7,20}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter a valid phone number (e.g. +1234567890).");
            return "redirect:/admin/users/" + id;
        }

        try {
            userService.updateUser(id, name.trim(), phone != null ? phone.trim() : null, address != null ? address.trim() : null);
            redirectAttributes.addFlashAttribute("successMessage", "User details updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update user: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long id,
                               @RequestParam(value = "redirect", required = false) String redirect,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "User account status changed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to change status: " + e.getMessage());
        }
        return "dashboard".equalsIgnoreCase(redirect)
                ? "redirect:/admin/dashboard"
                : "redirect:/admin/users";
    }

    @PostMapping("/{id}/assign-role")
    public String assignRole(@PathVariable("id") Long id,
                             @RequestParam("roleId") Long roleId,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.assignRole(id, roleId);
            redirectAttributes.addFlashAttribute("successMessage", "Role updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to assign role: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }
}
