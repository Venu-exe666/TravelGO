package com.travelgo.controller;

import com.travelgo.entity.Destination;
import com.travelgo.service.DestinationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for Destination management in Admin panel.
 */
@Controller
@RequestMapping("/staff/destinations")
public class PackageManagerDestinationController {

    private final DestinationService destinationService;

    public PackageManagerDestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @GetMapping
    public String listDestinations(Model model) {
        model.addAttribute("destinations", destinationService.findAll());
        return "staff/destinations";
    }

    @PostMapping("/create")
    public String createDestination(@RequestParam("country") String country,
                                   @RequestParam("city") String city,
                                   @RequestParam(value = "description", required = false) String description,
                                   @RequestParam(value = "image", required = false) String image,
                                   RedirectAttributes redirectAttributes) {
        try {
            Destination dest = new Destination();
            dest.setCountry(country);
            dest.setCity(city);
            dest.setDescription(description);
            dest.setImage(image);
            destinationService.save(dest);
            redirectAttributes.addFlashAttribute("successMessage", "Destination created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create destination: " + e.getMessage());
        }
        return "redirect:/staff/destinations";
    }

    @PostMapping("/{id}/edit")
    public String editDestination(@PathVariable("id") Long id,
                                 @RequestParam("country") String country,
                                 @RequestParam("city") String city,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam(value = "image", required = false) String image,
                                 RedirectAttributes redirectAttributes) {
        try {
            Destination dest = destinationService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Destination not found"));
            dest.setCountry(country);
            dest.setCity(city);
            dest.setDescription(description);
            dest.setImage(image);
            destinationService.save(dest);
            redirectAttributes.addFlashAttribute("successMessage", "Destination updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update destination: " + e.getMessage());
        }
        return "redirect:/staff/destinations";
    }

    @PostMapping("/{id}/delete")
    public String deleteDestination(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            destinationService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Destination deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete destination: " + e.getMessage());
        }
        return "redirect:/staff/destinations";
    }
}
