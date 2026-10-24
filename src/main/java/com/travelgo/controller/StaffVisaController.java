package com.travelgo.controller;

import com.travelgo.entity.Payment;
import com.travelgo.entity.VisaApplication;
import com.travelgo.service.PaymentService;
import com.travelgo.service.VisaApplicationService;
import com.travelgo.service.VisaDocumentService;
import com.travelgo.service.BookingService;
import com.travelgo.service.EmailService;
import com.travelgo.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller for Staff Visa management.
 * Visa Officers can verify documents, set charges, process, approve, and reject visas.
 */
@Controller
@RequestMapping("/staff/visas")
public class StaffVisaController {

    private final VisaApplicationService visaApplicationService;
    private final VisaDocumentService visaDocumentService;
    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public StaffVisaController(VisaApplicationService visaApplicationService,
                              VisaDocumentService visaDocumentService,
                              PaymentService paymentService,
                              BookingService bookingService,
                              NotificationService notificationService,
                              EmailService emailService) {
        this.visaApplicationService = visaApplicationService;
        this.visaDocumentService = visaDocumentService;
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @GetMapping
    public String listVisas(Model model) {
        List<VisaApplication> apps = visaApplicationService.findAll();
        Map<Long, List<com.travelgo.entity.VisaDocument>> documentsMap = new HashMap<>();
        for (VisaApplication app : apps) {
            documentsMap.put(app.getId(), visaDocumentService.findByVisaApplicationId(app.getId()));
        }
        model.addAttribute("visaApplications", apps);
        model.addAttribute("documentsMap", documentsMap);
        return "staff/visas";
    }

    @PostMapping("/documents/{docId}/verify")
    public String verifyDocument(@PathVariable("docId") Long docId,
                                 @RequestParam(value = "applicationId", required = false) Long applicationId,
                                 @RequestParam(value = "redirect", required = false) String redirect,
                                 RedirectAttributes redirectAttributes) {
        try {
            visaDocumentService.verifyDocument(docId);
            redirectAttributes.addFlashAttribute("successMessage", "Document verified successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to verify document: " + e.getMessage());
        }
        return redirectAfterAction(redirect, applicationId);
    }

    @PostMapping("/{id}/verify-all")
    public String verifyAllDocuments(@PathVariable("id") Long id,
                                    @RequestParam(value = "redirect", required = false) String redirect,
                                    RedirectAttributes redirectAttributes) {
        try {
            visaApplicationService.markDocumentsVerified(id);
            redirectAttributes.addFlashAttribute("successMessage", "Documents marked as verified.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to mark documents as verified: " + e.getMessage());
        }
        return redirectAfterAction(redirect, id);
    }

    @PostMapping("/{id}/charges")
    public String setCharges(@PathVariable("id") Long id,
                            @RequestParam("visaCharge") BigDecimal visaCharge,
                            @RequestParam("documentationCharge") BigDecimal documentationCharge,
                            @RequestParam(value = "redirect", required = false) String redirect,
                            RedirectAttributes redirectAttributes) {
        try {
            visaApplicationService.setVisaCharges(id, visaCharge, documentationCharge);
            redirectAttributes.addFlashAttribute("successMessage", "Visa charges issued successfully according to package specifications.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to set charges: " + e.getMessage());
        }
        return redirectAfterAction(redirect, id);
    }

    @PostMapping("/{id}/request-documents")
    public String requestDocuments(@PathVariable("id") Long id,
                                   @RequestParam("remarks") String remarks,
                                   @RequestParam(value = "redirect", required = false) String redirect,
                                   RedirectAttributes redirectAttributes) {
        try {
            visaApplicationService.requestAdditionalDocuments(id, remarks);
            redirectAttributes.addFlashAttribute("successMessage", "Document request sent to applicant successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to request documents: " + e.getMessage());
        }
        return redirectAfterAction(redirect, id);
    }

    @PostMapping("/{id}/process")
    public String processVisa(@PathVariable("id") Long id,
                             @RequestParam(value = "redirect", required = false) String redirect,
                             RedirectAttributes redirectAttributes) {
        try {
            visaApplicationService.startProcessing(id);
            redirectAttributes.addFlashAttribute("successMessage", "Visa processing started.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to process visa: " + e.getMessage());
        }
        return redirectAfterAction(redirect, id);
    }

    @PostMapping("/{id}/approve")
    public String approveVisa(@PathVariable("id") Long id,
                              @RequestParam(value = "redirect", required = false) String redirect,
                              RedirectAttributes redirectAttributes) {
        try {
            visaApplicationService.approveVisa(id);
            redirectAttributes.addFlashAttribute("successMessage", "Visa approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve visa: " + e.getMessage());
        }
        return redirectAfterAction(redirect, id);
    }

    @PostMapping("/{id}/reject")
    public String rejectVisa(@PathVariable("id") Long id,
                            @RequestParam(value = "rejectionReason", required = false) String reason,
                            @RequestParam(value = "redirect", required = false) String redirect,
                            RedirectAttributes redirectAttributes) {
        try {
            VisaApplication visa = visaApplicationService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Visa application not found"));

            // Find the original payment for refund processing
            Payment originalPayment = paymentService.findByBookingId(visa.getBooking().getId())
                    .stream().findFirst().orElse(null);

            visaApplicationService.rejectVisa(id, reason, originalPayment);
            redirectAttributes.addFlashAttribute("successMessage", "Visa rejected. Booking cancelled and documentation charge refunded.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reject visa: " + e.getMessage());
        }
        return redirectAfterAction(redirect, id);
    }

    @PostMapping("/{id}/issue-tickets")
    public String issueTickets(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            VisaApplication visa = visaApplicationService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Visa application not found"));
            var booking = bookingService.issueTickets(visa.getBooking().getId());
            String downloadPath = "/customer/bookings/" + booking.getId() + "/ticket.pdf";
            String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(downloadPath).build().toUriString();
            notificationService.send(booking.getUser(), "Your tickets are ready",
                    "Booking #TG-" + booking.getId() + " has been released. Open the booking to view or download the ticket PDF.");
            try {
                emailService.sendTicketReleaseEmail(booking, downloadUrl);
                redirectAttributes.addFlashAttribute("successMessage", "Tickets and booking details were released and emailed to the customer.");
            } catch (RuntimeException emailFailure) {
                redirectAttributes.addFlashAttribute("successMessage", "Tickets were released and the customer was notified in the app. Email delivery could not be completed.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to issue tickets: " + e.getMessage());
        }
        return "redirect:/staff/visas";
    }

    private String redirectAfterAction(String redirect, Long applicationId) {
        if ("dashboard".equalsIgnoreCase(redirect)) {
            return "redirect:/staff/visa-dashboard?applicationId=" + applicationId;
        }
        return "redirect:/staff/visas";
    }
}
