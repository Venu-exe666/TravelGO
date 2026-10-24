package com.travelgo.controller;

import com.travelgo.entity.TourPackage;
import com.travelgo.service.DestinationService;
import com.travelgo.service.PackageCategoryService;
import com.travelgo.service.TourPackageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * Controller for Tour Package management in Admin panel.
 */
@Controller
@RequestMapping("/staff/packages")
public class PackageManagerTourPackageController {

    private final TourPackageService tourPackageService;
    private final DestinationService destinationService;
    private final PackageCategoryService categoryService;

    public PackageManagerTourPackageController(TourPackageService tourPackageService,
                                     DestinationService destinationService,
                                     PackageCategoryService categoryService) {
        this.tourPackageService = tourPackageService;
        this.destinationService = destinationService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listPackages(Model model) {
        model.addAttribute("packages", tourPackageService.findAll());
        model.addAttribute("destinations", destinationService.findAll());
        model.addAttribute("categories", categoryService.findAll());
        return "staff/packages";
    }

    @PostMapping("/create")
    public String createPackage(@RequestParam("name") String name,
                               @RequestParam("categoryId") Long categoryId,
                               @RequestParam("destinationId") Long destinationId,
                               @RequestParam(value = "description", required = false) String description,
                               @RequestParam("basePrice") BigDecimal basePrice,
                               @RequestParam("durationDays") Integer durationDays,
                               @RequestParam(value = "flightDetails", required = false) String flightDetails,
                               @RequestParam(value = "includedServices", required = false) String includedServices,
                               @RequestParam("maxCapacity") Integer maxCapacity,
                               @RequestParam(value = "image", required = false) String image,
                               RedirectAttributes redirectAttributes) {
        try {
            // Validate: no negative or zero amounts
            if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Base price must be a positive amount.");
                return "redirect:/staff/packages";
            }
            if (durationDays == null || durationDays <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Duration days must be a positive number.");
                return "redirect:/staff/packages";
            }
            if (maxCapacity == null || maxCapacity <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Max capacity must be a positive number.");
                return "redirect:/staff/packages";
            }
            TourPackage pkg = new TourPackage();
            pkg.setName(name);
            pkg.setCategory(categoryService.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found")));
            pkg.setDestination(destinationService.findById(destinationId)
                    .orElseThrow(() -> new IllegalArgumentException("Destination not found")));
            pkg.setDescription(description);
            pkg.setBasePrice(basePrice);
            pkg.setDurationDays(durationDays);
            pkg.setFlightDetails(flightDetails);
            pkg.setIncludedServices(includedServices);
            pkg.setMaxCapacity(maxCapacity);
            pkg.setImage(image);
            tourPackageService.save(pkg);
            redirectAttributes.addFlashAttribute("successMessage", "Tour package created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create package: " + e.getMessage());
        }
        return "redirect:/staff/packages";
    }

    @PostMapping("/{id}/edit")
    public String editPackage(@PathVariable("id") Long id,
                             @RequestParam("name") String name,
                             @RequestParam("categoryId") Long categoryId,
                             @RequestParam("destinationId") Long destinationId,
                             @RequestParam(value = "description", required = false) String description,
                             @RequestParam("basePrice") BigDecimal basePrice,
                             @RequestParam("durationDays") Integer durationDays,
                             @RequestParam(value = "flightDetails", required = false) String flightDetails,
                             @RequestParam(value = "includedServices", required = false) String includedServices,
                             @RequestParam("maxCapacity") Integer maxCapacity,
                             @RequestParam(value = "image", required = false) String image,
                             RedirectAttributes redirectAttributes) {
        try {
            // Validate: no negative or zero amounts
            if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Base price must be a positive amount.");
                return "redirect:/staff/packages";
            }
            if (durationDays == null || durationDays <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Duration days must be a positive number.");
                return "redirect:/staff/packages";
            }
            if (maxCapacity == null || maxCapacity <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Max capacity must be a positive number.");
                return "redirect:/staff/packages";
            }
            TourPackage pkg = tourPackageService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Package not found"));
            pkg.setName(name);
            pkg.setCategory(categoryService.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found")));
            pkg.setDestination(destinationService.findById(destinationId)
                    .orElseThrow(() -> new IllegalArgumentException("Destination not found")));
            pkg.setDescription(description);
            pkg.setBasePrice(basePrice);
            pkg.setDurationDays(durationDays);
            pkg.setFlightDetails(flightDetails);
            pkg.setIncludedServices(includedServices);
            pkg.setMaxCapacity(maxCapacity);
            pkg.setImage(image);
            tourPackageService.save(pkg);
            redirectAttributes.addFlashAttribute("successMessage", "Tour package updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update package: " + e.getMessage());
        }
        return "redirect:/staff/packages";
    }

    @PostMapping("/{id}/toggle-status")
    public String togglePackageStatus(@PathVariable("id") Long id,
                                      @RequestParam(value = "redirect", required = false) String redirect,
                                      RedirectAttributes redirectAttributes) {
        try {
            TourPackage pkg = tourPackageService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Package not found"));
            pkg.setActive(!pkg.isActive());
            tourPackageService.save(pkg);
            String status = pkg.isActive() ? "Available" : "Unavailable";
            redirectAttributes.addFlashAttribute("successMessage",
                "Package '" + pkg.getName() + "' marked as " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to toggle status: " + e.getMessage());
        }
        return "dashboard".equalsIgnoreCase(redirect)
                ? "redirect:/staff/package-dashboard"
                : "redirect:/staff/packages";
    }

    @PostMapping("/{id}/delete")
    public String deletePackage(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            tourPackageService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Tour package deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete package: " + e.getMessage());
        }
        return "redirect:/staff/packages";
    }
}
