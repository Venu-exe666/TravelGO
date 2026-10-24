package com.travelgo.controller;

import com.travelgo.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for Staff Booking management.
 * Travel Consultants and Visa Officers can view and confirm bookings.
 */
@Controller
@RequestMapping("/staff/bookings")
public class StaffBookingController {

    private final BookingService bookingService;

    public StaffBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public String listBookings(Model model) {
        model.addAttribute("bookings", bookingService.findAll());
        return "staff/bookings";
    }

    @PostMapping("/{id}/approve")
    public String approveBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.approveBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve booking: " + e.getMessage());
        }
        return "redirect:/staff/bookings";
    }
}
