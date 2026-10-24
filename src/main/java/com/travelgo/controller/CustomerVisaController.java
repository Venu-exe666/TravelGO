package com.travelgo.controller;

import com.travelgo.entity.Booking;
import com.travelgo.entity.User;
import com.travelgo.entity.VisaApplication;
import com.travelgo.entity.VisaDocument;
import com.travelgo.enums.VisaType;
import com.travelgo.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.travelgo.enums.BookingStatus;
import com.travelgo.enums.VisaStatus;
import com.travelgo.entity.Payment;
import com.travelgo.enums.PaymentType;
import com.travelgo.enums.PaymentStatus;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for Customer Visa operations.
 * Enforces customer ownership on visa-related actions.
 */
@Controller
@RequestMapping("/customer/visas")
public class CustomerVisaController {

    private final VisaApplicationService visaApplicationService;
    private final VisaDocumentService visaDocumentService;
    private final BookingService bookingService;
    private final UserService userService;
    private final PaymentService paymentService;

    private static final String UPLOAD_DIR = "uploads/visa-documents/";

    public CustomerVisaController(VisaApplicationService visaApplicationService,
                                 VisaDocumentService visaDocumentService,
                                 BookingService bookingService,
                                 UserService userService,
                                 PaymentService paymentService) {
        this.visaApplicationService = visaApplicationService;
        this.visaDocumentService = visaDocumentService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    private User resolveUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.getUserByEmail(userDetails.getUsername()).orElse(null);
    }

    /**
     * and eligible bookings where a visa application hasn't been started yet.
     */
    @GetMapping
    public String listVisas(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/auth/login";

        List<VisaApplication> visas = visaApplicationService.findByUserId(user.getId());
        model.addAttribute("visaApplications", visas);
        model.addAttribute("currentUser", user);

        Map<Long, List<VisaDocument>> documentsMap = visas.stream()
                .collect(Collectors.toMap(VisaApplication::getId, v -> visaDocumentService.findByVisaApplicationId(v.getId())));
        model.addAttribute("documentsMap", documentsMap);

        // Find customer bookings that do not have a visa application yet
        Set<Long> visaBookingIds = visas.stream()
                .map(v -> v.getBooking().getId())
                .collect(Collectors.toSet());

        List<Booking> bookingsWithoutVisa = bookingService.findByUserId(user.getId()).stream()
                .filter(b -> !visaBookingIds.contains(b.getId()))
                .filter(b -> b.getBookingStatus() != BookingStatus.CANCELLED)
                .collect(Collectors.toList());

        model.addAttribute("bookingsWithoutVisa", bookingsWithoutVisa);
        return "customer/visas";
    }

    /**
     *    documentation charge, and sets status to PENDING_DOCUMENTS.
     * 6. Redirects to "/customer/visas" with success flash message.
     */
    @PostMapping("/apply")
    public String applyForVisa(@RequestParam("bookingId") Long bookingId,
                              @RequestParam("visaType") String visaType,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/auth/login";

        try {
            Booking booking = bookingService.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            // Ownership check
            if (!booking.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied.");
                return "redirect:/customer/visas";
            }

            // Duplicate check
            if (visaApplicationService.findByUserId(user.getId()).stream().anyMatch(v -> v.getBooking().getId().equals(bookingId))) {
                redirectAttributes.addFlashAttribute("errorMessage", "A visa application already exists for this booking.");
                return "redirect:/customer/visas";
            }

            VisaType type = VisaType.valueOf(visaType);
            visaApplicationService.createVisaApplication(booking, type);
            redirectAttributes.addFlashAttribute("successMessage", "Visa application submitted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to apply for visa: " + e.getMessage());
        }
        return "redirect:/customer/visas";
    }

    /**
     * and advances visa workflow status.
     */
    @PostMapping("/{id}/documents")
    public String uploadDocuments(@PathVariable("id") Long visaId,
                                @RequestParam("documentName") String documentName,
                                @RequestParam("file") MultipartFile file,
                                @RequestParam(value = "redirect", required = false) String redirect,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/auth/login";

        VisaApplication visa = null;
        try {
            visa = visaApplicationService.findById(visaId)
                    .orElseThrow(() -> new IllegalArgumentException("Visa application not found"));

            // Ownership check
            if (!visa.getBooking().getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied.");
                return "booking".equalsIgnoreCase(redirect) && visa.getBooking() != null
                        ? "redirect:/customer/bookings/" + visa.getBooking().getId()
                        : "redirect:/customer/visas";
            }

            if (documentName == null || documentName.trim().isEmpty() || documentName.trim().length() > 100) {
                redirectAttributes.addFlashAttribute("errorMessage", "Document label is required and cannot exceed 100 characters.");
                return "booking".equalsIgnoreCase(redirect) && visa.getBooking() != null
                        ? "redirect:/customer/bookings/" + visa.getBooking().getId()
                        : "redirect:/customer/visas";
            }

            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select a valid document file to upload.");
                return "booking".equalsIgnoreCase(redirect) && visa.getBooking() != null
                        ? "redirect:/customer/bookings/" + visa.getBooking().getId()
                        : "redirect:/customer/visas";
            }

            // Max file size: 10MB
            long maxSizeBytes = 10L * 1024 * 1024;
            if (file.getSize() > maxSizeBytes) {
                redirectAttributes.addFlashAttribute("errorMessage", "File size exceeds the allowable 10MB limit.");
                return "booking".equalsIgnoreCase(redirect) && visa.getBooking() != null
                        ? "redirect:/customer/bookings/" + visa.getBooking().getId()
                        : "redirect:/customer/visas";
            }

            // Validate allowed extensions
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().matches(".*\\.(pdf|jpg|jpeg|png)$")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid file format. Allowed formats are PDF, JPG, JPEG, and PNG.");
                return "booking".equalsIgnoreCase(redirect) && visa.getBooking() != null
                        ? "redirect:/customer/bookings/" + visa.getBooking().getId()
                        : "redirect:/customer/visas";
            }

            // Save file to disk
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // Create VisaDocument record
            VisaDocument doc = new VisaDocument();
            doc.setVisaApplication(visa);
            doc.setDocumentName(documentName);
            doc.setDocumentUrl(filePath.toString().replace("\\", "/"));
            visaDocumentService.save(doc);

            // Transition visa status to DOCUMENTS_SUBMITTED if still PENDING_DOCUMENTS
            if (visa.getStatus() == VisaStatus.PENDING_DOCUMENTS) {
                visa.setStatus(VisaStatus.DOCUMENTS_SUBMITTED);
                visaApplicationService.save(visa);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Document uploaded successfully.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "File upload failed: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload document: " + e.getMessage());
        }
        if ("booking".equalsIgnoreCase(redirect) && visa != null && visa.getBooking() != null) {
            return "redirect:/customer/bookings/" + visa.getBooking().getId();
        }
        return "redirect:/customer/visas";
    }
}
