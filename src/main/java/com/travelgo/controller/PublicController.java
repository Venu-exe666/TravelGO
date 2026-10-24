package com.travelgo.controller;

import com.travelgo.entity.ContactMessage;
import com.travelgo.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for all public-facing pages.
 * These pages are accessible without login.
 */
@Controller
public class PublicController {

    private final SystemSettingsService settingsService;
    private final DestinationService destinationService;
    private final TourPackageService tourPackageService;
    private final PackageCategoryService packageCategoryService;
    private final ContactMessageService contactMessageService;
    private final UserService userService;
    private final HotelService hotelService;

    public PublicController(SystemSettingsService settingsService,
                           DestinationService destinationService,
                           TourPackageService tourPackageService,
                           PackageCategoryService packageCategoryService,
                           ContactMessageService contactMessageService,
                           UserService userService,
                           HotelService hotelService) {
        this.settingsService = settingsService;
        this.destinationService = destinationService;
        this.tourPackageService = tourPackageService;
        this.packageCategoryService = packageCategoryService;
        this.contactMessageService = contactMessageService;
        this.userService = userService;
        this.hotelService = hotelService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("settings", settingsService.getSettings());
        model.addAttribute("destinations", destinationService.findAll());
        model.addAttribute("packages", tourPackageService.findAllActive());
        model.addAttribute("categories", packageCategoryService.findAll());
        return "index";
    }

    @GetMapping("/destinations")
    public String destinations(@RequestParam(value = "search", required = false) String search, Model model) {
        model.addAttribute("settings", settingsService.getSettings());
        if (search != null && !search.trim().isEmpty()) {
            model.addAttribute("destinations", destinationService.searchDestinations(search.trim()));
        } else {
            model.addAttribute("destinations", destinationService.findAll());
        }
        model.addAttribute("search", search);
        return "destinations";
    }

    @GetMapping("/packages")
    public String packages(@RequestParam(value = "search", required = false) String search,
                          @RequestParam(value = "destinationId", required = false) Long destinationId,
                          @RequestParam(value = "categoryId", required = false) Long categoryId,
                          Model model) {
        model.addAttribute("settings", settingsService.getSettings());
        model.addAttribute("packages", tourPackageService.filterActivePackages(search, destinationId, categoryId));
        model.addAttribute("destinations", destinationService.findAll());
        model.addAttribute("categories", packageCategoryService.findAll());
        model.addAttribute("search", search);
        model.addAttribute("selectedDestinationId", destinationId);
        model.addAttribute("selectedCategoryId", categoryId);
        return "packages";
    }

    @GetMapping("/packages/{id}/book")
    public String bookingForm(@PathVariable("id") Long packageId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        return tourPackageService.findById(packageId).map(pkg -> {
            model.addAttribute("tourPackage", pkg);
            model.addAttribute("hotels", hotelService.findByDestinationId(pkg.getDestination().getId()));
            model.addAttribute("settings", settingsService.getSettings());
            return "customer/booking-form";
        }).orElse("redirect:/packages");
    }

    @GetMapping("/visa-info")
    public String visaInfo(Model model) {
        model.addAttribute("settings", settingsService.getSettings());
        return "visa-info";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("settings", settingsService.getSettings());
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("settings", settingsService.getSettings());
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(@RequestParam("senderName") String senderName,
                               @RequestParam("senderEmail") String senderEmail,
                               @RequestParam("subject") String subject,
                               @RequestParam("message") String message,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        // Form field validation
        if (senderName == null || senderName.trim().length() < 2 || senderName.trim().length() > 100) {
            redirectAttributes.addFlashAttribute("errorMessage", "Full Name must be between 2 and 100 characters.");
            redirectAttributes.addFlashAttribute("senderName", senderName);
            redirectAttributes.addFlashAttribute("senderEmail", senderEmail);
            redirectAttributes.addFlashAttribute("subject", subject);
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/contact";
        }

        if (senderEmail == null || !senderEmail.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter a valid email address.");
            redirectAttributes.addFlashAttribute("senderName", senderName);
            redirectAttributes.addFlashAttribute("senderEmail", senderEmail);
            redirectAttributes.addFlashAttribute("subject", subject);
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/contact";
        }

        if (subject == null || subject.trim().isEmpty() || subject.trim().length() > 200) {
            redirectAttributes.addFlashAttribute("errorMessage", "Subject is required and cannot exceed 200 characters.");
            redirectAttributes.addFlashAttribute("senderName", senderName);
            redirectAttributes.addFlashAttribute("senderEmail", senderEmail);
            redirectAttributes.addFlashAttribute("subject", subject);
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/contact";
        }

        if (message == null || message.trim().length() < 10 || message.trim().length() > 5000) {
            redirectAttributes.addFlashAttribute("errorMessage", "Message must be between 10 and 5000 characters.");
            redirectAttributes.addFlashAttribute("senderName", senderName);
            redirectAttributes.addFlashAttribute("senderEmail", senderEmail);
            redirectAttributes.addFlashAttribute("subject", subject);
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/contact";
        }

        try {
            ContactMessage contactMessage = new ContactMessage();
            contactMessage.setSenderName(senderName.trim());
            contactMessage.setSenderEmail(senderEmail.trim());
            contactMessage.setSubject(subject.trim());
            contactMessage.setMessage(message.trim());

            // Link to user if authenticated
            if (userDetails != null) {
                userService.getUserByEmail(userDetails.getUsername())
                        .ifPresent(contactMessage::setUser);
            }

            contactMessageService.save(contactMessage);
            redirectAttributes.addFlashAttribute("successMessage", "Your message has been sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send message: " + e.getMessage());
        }
        return "redirect:/contact";
    }

    @GetMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("settings", settingsService.getSettings());
        return "faq";
    }

    @GetMapping("/maintenance")
    public String maintenance(Model model) {
        model.addAttribute("settings", settingsService.getSettings());
        return "maintenance";
    }
}
