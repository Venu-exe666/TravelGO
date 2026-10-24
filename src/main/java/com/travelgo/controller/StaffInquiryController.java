package com.travelgo.controller;

import com.travelgo.service.ContactMessageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for Staff Inquiry/Contact Message management.
 * Staff can view and reply to customer inquiries.
 */
@Controller
@RequestMapping("/staff/inquiries")
public class StaffInquiryController {

    private final ContactMessageService contactMessageService;

    public StaffInquiryController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    @GetMapping
    public String listInquiries(Model model) {
        model.addAttribute("inquiries", contactMessageService.findAll());
        return "staff/inquiries";
    }

    @PostMapping("/{id}/reply")
    public String replyToInquiry(@PathVariable("id") Long id,
                                @RequestParam("replyText") String replyText,
                                RedirectAttributes redirectAttributes) {
        try {
            contactMessageService.replyToMessage(id, replyText);
            redirectAttributes.addFlashAttribute("successMessage", "Reply sent successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send reply: " + e.getMessage());
        }
        return "redirect:/staff/inquiries";
    }
}
