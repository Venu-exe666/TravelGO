package com.travelgo.controller;

import com.travelgo.entity.User;
import com.travelgo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Customer-owned account and contact settings. */
@Controller
@RequestMapping("/customer/settings")
public class CustomerProfileController {

    private static final String PHONE_PATTERN = "^\\+?[0-9\\-\\s()]{7,20}$";
    private final UserService userService;

    public CustomerProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String settings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User customer = requireCustomer(userDetails);
        model.addAttribute("currentUser", customer);
        return "customer/settings";
    }

    @PostMapping
    public String updateSettings(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam("name") String name,
                                 @RequestParam(value = "phone", required = false) String phone,
                                 @RequestParam(value = "address", required = false) String address,
                                 RedirectAttributes redirectAttributes) {
        String cleanName = trimToNull(name);
        String cleanPhone = trimToNull(phone);
        String cleanAddress = trimToNull(address);

        if (cleanName == null || cleanName.length() < 2 || cleanName.length() > 100) {
            redirectAttributes.addFlashAttribute("errorMessage", "Full name must be between 2 and 100 characters.");
            return "redirect:/customer/settings";
        }
        if (cleanPhone != null && !cleanPhone.matches(PHONE_PATTERN)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Enter a valid phone number using 7 to 20 digits.");
            return "redirect:/customer/settings";
        }
        if (cleanAddress != null && cleanAddress.length() > 1000) {
            redirectAttributes.addFlashAttribute("errorMessage", "Address must not exceed 1,000 characters.");
            return "redirect:/customer/settings";
        }

        try {
            User customer = requireCustomer(userDetails);
            userService.updateCustomerProfile(customer.getEmail(), cleanName, cleanPhone, cleanAddress);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Your profile details were saved and are now available to the TravelGO support team.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "We could not save your profile: " + exception.getMessage());
        }
        return "redirect:/customer/settings";
    }

    private User requireCustomer(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalStateException("A signed-in customer is required.");
        }
        User user = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Signed-in customer account was not found."));
        if (user.getRole() == null || !"CUSTOMER".equals(user.getRole().getRoleName())) {
            throw new IllegalStateException("Customer profile settings are not available for this account.");
        }
        return user;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
