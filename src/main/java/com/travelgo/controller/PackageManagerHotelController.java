package com.travelgo.controller;

import com.travelgo.entity.Hotel;
import com.travelgo.service.DestinationService;
import com.travelgo.service.HotelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * Controller for Hotel management in Admin panel.
 */
@Controller
@RequestMapping("/staff/hotels")
public class PackageManagerHotelController {

    private final HotelService hotelService;
    private final DestinationService destinationService;

    public PackageManagerHotelController(HotelService hotelService, DestinationService destinationService) {
        this.hotelService = hotelService;
        this.destinationService = destinationService;
    }

    @GetMapping
    public String listHotels(Model model) {
        model.addAttribute("hotels", hotelService.findAll());
        model.addAttribute("destinations", destinationService.findAll());
        return "staff/hotels";
    }

    @PostMapping("/create")
    public String createHotel(@RequestParam("name") String name,
                             @RequestParam("destinationId") Long destinationId,
                             @RequestParam("address") String address,
                             @RequestParam(value = "starRating", required = false) Integer starRating,
                             @RequestParam("pricePerNight") BigDecimal pricePerNight,
                             @RequestParam(value = "description", required = false) String description,
                             @RequestParam(value = "image", required = false) String image,
                             RedirectAttributes redirectAttributes) {
        try {
            Hotel hotel = new Hotel();
            hotel.setName(name);
            hotel.setDestination(destinationService.findById(destinationId)
                    .orElseThrow(() -> new IllegalArgumentException("Destination not found")));
            hotel.setAddress(address);
            hotel.setStarRating(starRating);
            hotel.setPricePerNight(pricePerNight);
            hotel.setDescription(description);
            hotel.setImage(image);
            hotelService.save(hotel);
            redirectAttributes.addFlashAttribute("successMessage", "Hotel created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create hotel: " + e.getMessage());
        }
        return "redirect:/staff/hotels";
    }

    @PostMapping("/{id}/edit")
    public String editHotel(@PathVariable("id") Long id,
                           @RequestParam("name") String name,
                           @RequestParam("destinationId") Long destinationId,
                           @RequestParam("address") String address,
                           @RequestParam(value = "starRating", required = false) Integer starRating,
                           @RequestParam("pricePerNight") BigDecimal pricePerNight,
                           @RequestParam(value = "description", required = false) String description,
                           @RequestParam(value = "image", required = false) String image,
                           RedirectAttributes redirectAttributes) {
        try {
            Hotel hotel = hotelService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Hotel not found"));
            hotel.setName(name);
            hotel.setDestination(destinationService.findById(destinationId)
                    .orElseThrow(() -> new IllegalArgumentException("Destination not found")));
            hotel.setAddress(address);
            hotel.setStarRating(starRating);
            hotel.setPricePerNight(pricePerNight);
            hotel.setDescription(description);
            hotel.setImage(image);
            hotelService.save(hotel);
            redirectAttributes.addFlashAttribute("successMessage", "Hotel updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update hotel: " + e.getMessage());
        }
        return "redirect:/staff/hotels";
    }

    @PostMapping("/{id}/delete")
    public String deleteHotel(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            hotelService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Hotel deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete hotel: " + e.getMessage());
        }
        return "redirect:/staff/hotels";
    }
}
